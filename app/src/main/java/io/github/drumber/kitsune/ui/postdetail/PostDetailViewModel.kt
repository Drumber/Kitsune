package io.github.drumber.kitsune.ui.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.CommentRepository
import io.github.drumber.kitsune.data.repository.ContentRevealStore
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModel(
    private val commentRepository: CommentRepository,
    private val postManagementRepository: PostManagementRepository,
    private val postInteractionRepository: PostInteractionRepository,
    private val postInteractionStore: PostInteractionStore,
    private val contentRevealStore: ContentRevealStore,
    private val userRepository: UserRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data object CommentPosted : Event
        data object Error : Event
        data object PostDeleted : Event
        data object CommentUpdated : Event
        data object CommentDeleted : Event
        data class CommentLikeChanged(val commentId: String, val isLiked: Boolean, val count: Int) : Event
    }

    data class PostLikeUiState(
        val isLiked: Boolean = false,
        val count: Int = 0
    )

    /** Current state of the comment composer. Survives configuration changes. */
    sealed interface ComposerMode {
        data object Normal : ComposerMode
        data class Reply(val comment: Comment) : ComposerMode
        data class Edit(val comment: Comment) : ComposerMode
    }

    private val post = MutableStateFlow<Post?>(null)

    /** The current post, updated once the full version has been fetched from the network. */
    val postState = post.asStateFlow()

    private val _postLikeState = MutableStateFlow(PostLikeUiState())
    val postLikeState = _postLikeState.asStateFlow()
    private var postLikeId: String? = null

    private val _composerMode = MutableStateFlow<ComposerMode>(ComposerMode.Normal)
    val composerMode = _composerMode.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    // In-session tracking of comment like ids for unliking.
    private val commentLikeIds = mutableMapOf<String, String?>()
    private val commentLikedState = mutableMapOf<String, Boolean>()
    private val commentLikeCounts = mutableMapOf<String, Int>()

    /**
     * Fetches a post by its id and calls [setPost] once the result is available. Used by the
     * Compose navigation graph, which passes only the id as a route argument.
     */
    fun initFromPostId(postId: String) {
        if (post.value?.id == postId) return
        viewModelScope.launch {
            try {
                postManagementRepository.getPost(postId)?.let { setPost(it) }
            } catch (e: Exception) {
                logE("Failed to load post '$postId'.", e)
            }
        }
    }

    fun setPost(newPost: Post) {
        if (post.value?.id == newPost.id) return
        post.value = newPost
        _postLikeState.value = PostLikeUiState(isLiked = false, count = newPost.likesCount)
        // Some entry points (e.g. notifications) only carry a partial post without images,
        // media or embed. Re-fetch the full post so the detail screen renders completely.
        viewModelScope.launch {
            try {
                postManagementRepository.getPost(newPost.id)?.let { fullPost ->
                    post.value = fullPost
                    _postLikeState.value = _postLikeState.value.copy(count = fullPost.likesCount)
                }
            } catch (e: Exception) {
                logE("Failed to fetch full post '${newPost.id}'.", e)
            }
        }
        val userId = getLocalUserId() ?: return
        viewModelScope.launch {
            try {
                val likeId = postInteractionRepository.getMyPostLikeId(newPost.id, userId)
                postLikeId = likeId
                _postLikeState.value = _postLikeState.value.copy(isLiked = likeId != null)
            } catch (e: Exception) {
                logE("Failed to load post like state for post '${newPost.id}'.", e)
            }
        }
    }

    val comments: Flow<PagingData<Comment>> = post.filterNotNull().flatMapLatest { p ->
        commentRepository.commentsPager(p.id, getLocalUserId())
    }.cachedIn(viewModelScope)

    /** Post ids whose spoiler/NSFW content the user revealed during this session. */
    val revealedPosts = contentRevealStore.revealed

    /** Whether NSFW posts may be shown without gating, based on the user's SFW preference. */
    val nsfwAllowed: Boolean
        get() = userRepository.localUser.value?.sfwFilterPreference ==
            LocalSfwFilterPreference.NSFW_EVERYWHERE

    /** Remembers that the user revealed the gated content of the current post. */
    fun revealCurrentPost() {
        post.value?.let { contentRevealStore.reveal(it.id) }
    }

    fun togglePostLike() {
        val currentPost = post.value ?: return
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }

        val state = _postLikeState.value
        val targetLiked = !state.isLiked
        val targetCount = (state.count + if (targetLiked) 1 else -1).coerceAtLeast(0)
        // Optimistic update.
        _postLikeState.value = state.copy(
            isLiked = targetLiked,
            count = targetCount
        )
        postInteractionStore.setLikeState(currentPost.id, targetLiked, targetCount)

        viewModelScope.launch {
            try {
                if (targetLiked) {
                    postLikeId = postInteractionRepository.likePost(currentPost.id, userId)
                } else {
                    postLikeId?.let { postInteractionRepository.unlikePost(it) }
                    postLikeId = null
                }
            } catch (e: Exception) {
                logE("Failed to toggle like for post '${currentPost.id}'.", e)
                // Revert optimistic update.
                _postLikeState.value = state
                postInteractionStore.setLikeState(currentPost.id, state.isLiked, state.count)
                eventChannel.send(Event.Error)
            }
        }
    }

    fun toggleCommentLike(comment: Comment) {
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }

        val currentlyLiked = commentLikedState[comment.id] ?: comment.isLikedByMe
        val currentCount = commentLikeCounts[comment.id] ?: comment.likesCount
        val currentLikeId = if (commentLikeIds.containsKey(comment.id)) {
            commentLikeIds[comment.id]
        } else {
            comment.myLikeId
        }

        val targetLiked = !currentlyLiked
        val targetCount = (currentCount + if (targetLiked) 1 else -1).coerceAtLeast(0)

        // Optimistic update.
        commentLikedState[comment.id] = targetLiked
        commentLikeCounts[comment.id] = targetCount
        eventChannel.trySend(Event.CommentLikeChanged(comment.id, targetLiked, targetCount))

        viewModelScope.launch {
            try {
                if (targetLiked) {
                    val likeId = commentRepository.likeComment(comment.id, userId)
                    commentLikeIds[comment.id] = likeId
                } else {
                    currentLikeId?.let { commentRepository.unlikeComment(it) }
                    commentLikeIds[comment.id] = null
                }
            } catch (e: Exception) {
                logE("Failed to toggle like for comment '${comment.id}'.", e)
                // Revert optimistic update.
                commentLikedState[comment.id] = currentlyLiked
                commentLikeCounts[comment.id] = currentCount
                eventChannel.send(Event.CommentLikeChanged(comment.id, currentlyLiked, currentCount))
                eventChannel.send(Event.Error)
            }
        }
    }

    fun postComment(content: String) {
        val currentPost = post.value ?: return
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            try {
                val comment = commentRepository.postComment(currentPost.id, userId, trimmed)
                if (comment != null) {
                    val newCount = (postInteractionStore.get(currentPost.id)?.commentsCount
                        ?: currentPost.commentsCount) + 1
                    postInteractionStore.setCommentCount(currentPost.id, newCount)
                    eventChannel.send(Event.CommentPosted)
                } else {
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to post comment on post '${currentPost.id}'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

    fun postReply(parentCommentId: String, content: String) {
        val currentPost = post.value ?: return
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            try {
                val reply = commentRepository.postReply(
                    currentPost.id,
                    parentCommentId,
                    userId,
                    trimmed
                )
                if (reply != null) {
                    eventChannel.send(Event.CommentPosted)
                } else {
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to post reply to comment '$parentCommentId'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

    fun isLoggedIn() = getLocalUserId() != null

    /** Id of the currently signed-in user, or `null` when not logged in. */
    fun currentUserId(): String? = getLocalUserId()

    /** Switches the composer into reply mode for the given [comment]. */
    fun startReply(comment: Comment) {
        _composerMode.value = ComposerMode.Reply(comment)
    }

    /** Switches the composer into edit mode for the given [comment]. */
    fun startEditComment(comment: Comment) {
        _composerMode.value = ComposerMode.Edit(comment)
    }

    /** Resets the composer back to posting a new top-level comment. */
    fun cancelComposer() {
        _composerMode.value = ComposerMode.Normal
    }

    /** Deletes the current post. Emits [Event.PostDeleted] on success. */
    fun deletePost() {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            try {
                postManagementRepository.deletePost(currentPost.id)
                eventChannel.send(Event.PostDeleted)
            } catch (e: Exception) {
                logE("Failed to delete post '${currentPost.id}'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

    /** Updates the content of a comment or reply owned by the user. */
    fun updateComment(commentId: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            try {
                val updated = commentRepository.updateComment(commentId, trimmed)
                if (updated != null) {
                    eventChannel.send(Event.CommentUpdated)
                } else {
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to update comment '$commentId'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

    /** Deletes a comment or reply owned by the user. */
    fun deleteComment(commentId: String) {
        val currentPost = post.value
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(commentId)
                if (currentPost != null) {
                    val newCount = ((postInteractionStore.get(currentPost.id)?.commentsCount
                        ?: currentPost.commentsCount) - 1).coerceAtLeast(0)
                    postInteractionStore.setCommentCount(currentPost.id, newCount)
                }
                eventChannel.send(Event.CommentDeleted)
            } catch (e: Exception) {
                logE("Failed to delete comment '$commentId'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

}
