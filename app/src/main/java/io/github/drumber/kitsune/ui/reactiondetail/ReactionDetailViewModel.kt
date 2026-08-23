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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReactionDetailViewModel(
    private val reactionId: String,
    private val mediaReactionRepository: MediaReactionRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data class UpvoteSuccess(val newCount: Int) : Event
        data object UpvoteFailed : Event
        data object UpdateSuccess : Event
        data object UpdateFailed : Event
        data object DeleteSuccess : Event
        data object DeleteFailed: Event
    }

    private val _reaction = MutableStateFlow<MediaReaction?>(null)
    val reaction = _reaction.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isUpvoted = MutableStateFlow(false)
    val isUpvoted = _isUpvoted.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    init {
        loadReaction()
    }

    private fun loadReaction() {
        viewModelScope.launch {
            _isLoading.update { true }
            try {
                _reaction.update { mediaReactionRepository.getReaction(reactionId) }
            } catch (e: Exception) {
                logE("Failed to load reaction with id '$reactionId'.", e)
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun currentUserId() = getLocalUserId()

    fun upvote() {
        if (_isUpvoted.value) return
        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }
        val current = _reaction.value ?: return
        viewModelScope.launch {
            val event = try {
                if (mediaReactionRepository.upvoteReaction(userId, reactionId)) {
                    val newCount = current.upVotesCount + 1
                    _isUpvoted.value = true
                    _reaction.value = current.copy(upVotesCount = newCount)
                    Event.UpvoteSuccess(newCount)
                } else {
                    Event.UpvoteFailed
                }
            } catch (e: Exception) {
                logE("Failed to upvote reaction with id '$reactionId'.", e)
                Event.UpvoteFailed
            }
            eventChannel.send(event)
        }
    }

    fun updateReaction(reaction: MediaReaction, text: String) {
        val reactionText = text.trim()
        if (reactionText.isEmpty()) return
        viewModelScope.launch {
            val event = try {
                val updatedReaction = mediaReactionRepository.updateReaction(reaction.id, reactionText)
                updatedReaction?.let {
                    _reaction.update { it }
                    Event.UpdateSuccess
                } ?: Event.UpdateFailed
            } catch (e: Exception) {
                logE("Failed to update reaction with id '${reaction.id}'.", e)
                Event.UpdateFailed
            }
            eventChannel.send(event)
        }
    }

    fun deleteReaction(reaction: MediaReaction) {
        if (getLocalUserId() == null) {
            return
        }
        viewModelScope.launch {
            val event = try {
                mediaReactionRepository.deleteReaction(reaction.id)
                Event.DeleteSuccess
            } catch (e: Exception) {
                logE("Failed to delete reaction with id '${reaction.id}'.", e)
                Event.DeleteFailed
            }
            eventChannel.send(event)
        }
    }
}
