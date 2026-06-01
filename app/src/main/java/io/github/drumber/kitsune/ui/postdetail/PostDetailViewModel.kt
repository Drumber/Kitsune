package io.github.drumber.kitsune.ui.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.CommentRepository
import io.github.drumber.kitsune.data.repository.PostInteractionRepository
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
    private val postInteractionRepository: PostInteractionRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data object CommentPosted : Event
        data object Error : Event
        data class CommentLikeChanged(val commentId: String, val isLiked: Boolean, val count: Int) : Event
    }

    data class PostLikeUiState(
        val isLiked: Boolean = false,
        val count: Int = 0
    )

    private val post = MutableStateFlow<Post?>(null)

    private val _postLikeState = MutableStateFlow(PostLikeUiState())
    val postLikeState = _postLikeState.asStateFlow()
    private var postLikeId: String? = null

    private val _commentsRefresh = MutableStateFlow(false)
    val commentsRefresh = _commentsRefresh.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    // In-session tracking of comment like ids for unliking.
    private val commentLikeIds = mutableMapOf<String, String?>()
    private val commentLikedState = mutableMapOf<String, Boolean>()
    private val commentLikeCounts = mutableMapOf<String, Int>()

    fun setPost(newPost: Post) {
        if (post.value?.id == newPost.id) return
        post.value = newPost
        _postLikeState.value = PostLikeUiState(isLiked = false, count = newPost.likesCount)
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

    fun togglePostLike() {
        val currentPost = post.value ?: return
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }

        val state = _postLikeState.value
        val targetLiked = !state.isLiked
        // Optimistic update.
        _postLikeState.value = state.copy(
            isLiked = targetLiked,
            count = (state.count + if (targetLiked) 1 else -1).coerceAtLeast(0)
        )

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

    fun isLoggedIn() = getLocalUserId() != null

}
