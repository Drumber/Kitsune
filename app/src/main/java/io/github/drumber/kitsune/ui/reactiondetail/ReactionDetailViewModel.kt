package io.github.drumber.kitsune.ui.reactiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReactionDetailViewModel(
    private val reactionId: String,
    private val mediaReactionRepository: MediaReactionRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface UpvoteEvent {
        data object LoginRequired : UpvoteEvent
        data class Success(val newCount: Int) : UpvoteEvent
        data object Failed : UpvoteEvent
    }

    private val _reaction = MutableStateFlow<MediaReaction?>(null)
    val reaction = _reaction.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpvoted = MutableStateFlow(false)
    val isUpvoted = _isUpvoted.asStateFlow()

    private val upvoteEventChannel = Channel<UpvoteEvent>(Channel.BUFFERED)
    val upvoteEvents: Flow<UpvoteEvent> = upvoteEventChannel.receiveAsFlow()

    init {
        loadReaction()
    }

    private fun loadReaction() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _reaction.value = mediaReactionRepository.getReaction(reactionId)
            } catch (e: Exception) {
                logE("Failed to load reaction with id '$reactionId'.", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun upvote() {
        if (_isUpvoted.value) return
        val userId = getLocalUserId()
        if (userId == null) {
            upvoteEventChannel.trySend(UpvoteEvent.LoginRequired)
            return
        }
        val current = _reaction.value ?: return
        viewModelScope.launch {
            val event = try {
                if (mediaReactionRepository.upvoteReaction(userId, reactionId)) {
                    val newCount = current.upVotesCount + 1
                    _isUpvoted.value = true
                    _reaction.value = current.copy(upVotesCount = newCount)
                    UpvoteEvent.Success(newCount)
                } else {
                    UpvoteEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to upvote reaction with id '$reactionId'.", e)
                UpvoteEvent.Failed
            }
            upvoteEventChannel.send(event)
        }
    }

}
