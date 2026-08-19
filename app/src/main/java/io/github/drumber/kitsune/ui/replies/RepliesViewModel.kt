package io.github.drumber.kitsune.ui.replies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.repository.CommentRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RepliesViewModel(
    private val parentCommentId: String,
    private val postId: String,
    private val commentRepository: CommentRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data object Error : Event
        data object ReplyPosted : Event
        data class ReplyUpdated(val isParentComment: Boolean) : Event
        data class CommentLikeChanged(val commentId: String, val isLiked: Boolean, val count: Int) : Event
        data class CommentDeleted(val isParentComment: Boolean) : Event
    }

    sealed interface ComposerMode {
        data object Normal : ComposerMode
        data class Edit(val comment: Comment) : ComposerMode
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    /** The comment whose replies are being shown, or `null` until it has loaded. */
    private val _parentComment = MutableStateFlow<Comment?>(null)
    val parentComment: StateFlow<Comment?> = _parentComment.asStateFlow()

    /** Fully paginated list of the parent comment's replies, oldest first. */
    val replies: Flow<PagingData<Comment>> =
        commentRepository.repliesPager(parentCommentId, getLocalUserId()).cachedIn(viewModelScope)

    private val _composerMode = MutableStateFlow<ComposerMode>(ComposerMode.Normal)
    val composerMode = _composerMode.asStateFlow()

    // In-session tracking of reply like ids for unliking.
    private val commentLikeIds = mutableMapOf<String, String?>()
    private val commentLikedState = mutableMapOf<String, Boolean>()
    private val commentLikeCounts = mutableMapOf<String, Int>()

    init {
        loadParentComment()
    }

    private fun loadParentComment() {
        viewModelScope.launch {
            try {
                _parentComment.value = commentRepository.getComment(parentCommentId, getLocalUserId())
            } catch (e: Exception) {
                logE("Failed to load parent comment '$parentCommentId'.", e)
            }
        }
    }

    /** Id of the currently signed-in user, or `null` when not logged in. */
    fun currentUserId(): String? = getLocalUserId()

    /** Posts a new reply to the parent comment. Emits [Event.ReplyPosted] on success. */
    fun postReply(content: String) {
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            try {
                val reply = commentRepository.postReply(postId, parentCommentId, userId, trimmed)
                if (reply != null) {
                    eventChannel.send(Event.ReplyPosted)
                } else {
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to post reply to comment '$parentCommentId'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }

    fun updateComment(commentId: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        val isParentComment = commentId == parentCommentId
        viewModelScope.launch {
            try {
                val updated = commentRepository.updateComment(commentId, trimmed)
                if (updated != null) {
                    if (isParentComment) {
                        _parentComment.update { updated }
                    }
                    eventChannel.send(Event.ReplyUpdated(isParentComment))
                } else {
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to update comment '$commentId'.", e)
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
                logE("Failed to toggle like for reply '${comment.id}'.", e)
                // Revert optimistic update.
                commentLikedState[comment.id] = currentlyLiked
                commentLikeCounts[comment.id] = currentCount
                eventChannel.send(Event.CommentLikeChanged(comment.id, currentlyLiked, currentCount))
                eventChannel.send(Event.Error)
            }
        }
    }

    fun startEditComment(comment: Comment) {
        _composerMode.update { ComposerMode.Edit(comment) }
    }

    fun cancelComposer() {
        _composerMode.update { ComposerMode.Normal }
    }

    fun deleteComment(commentId: String) {
        val isParentComment = commentId == parentCommentId
        viewModelScope.launch {
            try {
                commentRepository.deleteComment(commentId)
                eventChannel.send(Event.CommentDeleted(isParentComment))
            } catch (e: Exception) {
                logE("Failed to delete comment '$commentId'.", e)
                eventChannel.send(Event.Error)
            }
        }
    }
}
