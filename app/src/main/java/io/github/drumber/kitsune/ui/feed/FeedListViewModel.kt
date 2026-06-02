package io.github.drumber.kitsune.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.CommentRepository
import io.github.drumber.kitsune.data.repository.ContentRevealStore
import io.github.drumber.kitsune.data.repository.FeedRepository
import io.github.drumber.kitsune.data.repository.PostInteractionRepository
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.data.repository.PostManagementRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModel(
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val postManagementRepository: PostManagementRepository,
    private val postInteractionRepository: PostInteractionRepository,
    private val postInteractionStore: PostInteractionStore,
    private val contentRevealStore: ContentRevealStore,
    private val userRepository: UserRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface LikeEvent {
        data object LoginRequired : LikeEvent
        data class Failed(val postId: String, val isLiked: Boolean, val count: Int) : LikeEvent
    }

    sealed interface ActionEvent {
        data object PostDeleted : ActionEvent
        data object Error : ActionEvent
    }

    private val actionEventChannel = Channel<ActionEvent>(Channel.BUFFERED)
    val actionEvents: Flow<ActionEvent> = actionEventChannel.receiveAsFlow()

    /** Id of the currently signed-in user, or `null` when not logged in. */
    fun currentUserId(): String? = getLocalUserId()

    /** Deletes the given post owned by the user. Emits [ActionEvent.PostDeleted] on success. */
    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postManagementRepository.deletePost(post.id)
                actionEventChannel.send(ActionEvent.PostDeleted)
            } catch (e: Exception) {
                logE("Failed to delete post '${post.id}'.", e)
                actionEventChannel.send(ActionEvent.Error)
            }
        }
    }

    private val feedType = MutableStateFlow<FeedType?>(null)

    /** Target user id for [FeedType.USER] feeds. */
    private var userFeedId: String? = null

    // Cache of liker avatar urls keyed by post id, to avoid refetching on rebind.
    private val likerAvatarCache = mutableMapOf<String, List<String>>()
    private val likerAvatarMutex = Mutex()

    // Known like ids keyed by post id, used to unlike without a lookup.
    private val postLikeIds = mutableMapOf<String, String?>()

    // Posts whose initial like state has already been resolved.
    private val likeStateLoaded = mutableSetOf<String>()
    private val likeStateMutex = Mutex()

    private val likeEventChannel = Channel<LikeEvent>(Channel.BUFFERED)
    val likeEvents: Flow<LikeEvent> = likeEventChannel.receiveAsFlow()

    /** Shared interaction overrides (like state, comment count) keyed by post id. */
    val interactionStates = postInteractionStore.states

    /** Post ids whose spoiler/NSFW content the user revealed during this session. */
    val revealedPosts = contentRevealStore.revealed

    /** Whether NSFW posts may be shown without gating, based on the user's SFW preference. */
    val nsfwAllowed: Boolean
        get() = userRepository.localUser.value?.sfwFilterPreference ==
            LocalSfwFilterPreference.NSFW_EVERYWHERE

    /** Remembers that the user revealed the gated content of the given post. */
    fun revealPost(post: Post) {
        contentRevealStore.reveal(post.id)
    }

    fun setFeedType(type: FeedType) {
        if (feedType.value != type) {
            feedType.value = type
        }
    }

    /** Configures this list to show the profile feed of the given user. */
    fun setUserFeed(userId: String) {
        userFeedId = userId
        if (feedType.value != FeedType.USER) {
            feedType.value = FeedType.USER
        }
    }

    /** Emits `true` if the current feed requires the user to be logged in but they are not. */
    val loginRequired: Flow<Boolean> = feedType.filterNotNull().map { type ->
        type == FeedType.FOLLOWING && getLocalUserId() == null
    }

    val dataSource: Flow<PagingData<Post>> = feedType.filterNotNull().flatMapLatest { type ->
        when (type) {
            FeedType.GLOBAL -> feedRepository.globalFeedPager()
            FeedType.FOLLOWING -> {
                val userId = getLocalUserId()
                if (userId == null) {
                    flowOf(PagingData.empty())
                } else {
                    feedRepository.timelineFeedPager(userId)
                }
            }
            FeedType.USER -> {
                val userId = userFeedId
                if (userId == null) {
                    flowOf(PagingData.empty())
                } else {
                    feedRepository.userFeedPager(userId)
                }
            }
        }
    }.cachedIn(viewModelScope)

    /**
     * Returns up to three distinct avatar urls of users who liked the given post, fetching them
     * lazily and caching the result. Returns an empty list if the post has no likes or on failure.
     */
    suspend fun likerAvatars(post: Post): List<String> {
        if (post.likesCount <= 0) return emptyList()
        likerAvatarCache[post.id]?.let { return it }
        return likerAvatarMutex.withLock {
            likerAvatarCache[post.id]?.let { return it }
            val avatars = try {
                postInteractionRepository.getTopLikerAvatars(post.id)
            } catch (e: Exception) {
                logE("Failed to load liker avatars for post '${post.id}'.", e)
                emptyList()
            }
            likerAvatarCache[post.id] = avatars
            avatars
        }
    }

    /**
     * Resolves the current user's like state for the given post once and, if the post is already
     * liked, pushes the filled state to the shared interaction store. No-op when not logged in.
     */
    suspend fun ensureLikeStateLoaded(post: Post) {
        val userId = getLocalUserId() ?: return
        if (post.id in likeStateLoaded) return
        likeStateMutex.withLock {
            if (post.id in likeStateLoaded) return
            // Don't clobber a like state the user already changed in this session.
            if (postInteractionStore.get(post.id)?.isLiked != null) {
                likeStateLoaded.add(post.id)
                return
            }
            try {
                val likeId = postInteractionRepository.getMyPostLikeId(post.id, userId)
                likeStateLoaded.add(post.id)
                if (likeId != null) {
                    postLikeIds[post.id] = likeId
                    postInteractionStore.setLikeState(post.id, true, post.likesCount)
                }
            } catch (e: Exception) {
                logE("Failed to load like state for post '${post.id}'.", e)
            }
        }
    }

    /**
     * Toggles the like state of the given post on behalf of the user. The UI is expected to have
     * already applied the optimistic [targetLiked] state; on failure a [LikeEvent.Failed] event is
     * emitted carrying the previous state so the UI can revert.
     */
    fun togglePostLike(post: Post, targetLiked: Boolean) {
        val userId = getLocalUserId()
        if (userId == null) {
            likeEventChannel.trySend(LikeEvent.LoginRequired)
            // Revert the optimistic change applied by the UI.
            likeEventChannel.trySend(LikeEvent.Failed(post.id, false, post.likesCount))
            return
        }

        val currentCount = postInteractionStore.get(post.id)?.likesCount ?: post.likesCount
        val targetCount = (currentCount + if (targetLiked) 1 else -1).coerceAtLeast(0)
        postInteractionStore.setLikeState(post.id, targetLiked, targetCount)

        viewModelScope.launch {
            try {
                if (targetLiked) {
                    postLikeIds[post.id] = postInteractionRepository.likePost(post.id, userId)
                } else {
                    val likeId = postLikeIds[post.id]
                        ?: postInteractionRepository.getMyPostLikeId(post.id, userId)
                    likeId?.let { postInteractionRepository.unlikePost(it) }
                    postLikeIds[post.id] = null
                }
            } catch (e: Exception) {
                logE("Failed to toggle like for post '${post.id}'.", e)
                postInteractionStore.setLikeState(post.id, !targetLiked, post.likesCount)
                likeEventChannel.send(
                    LikeEvent.Failed(post.id, !targetLiked, post.likesCount)
                )
            }
        }
    }

}
