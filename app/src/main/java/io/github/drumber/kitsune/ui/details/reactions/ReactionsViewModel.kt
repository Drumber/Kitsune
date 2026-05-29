package io.github.drumber.kitsune.ui.details.reactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionsViewModel(
    private val mediaReactionRepository: MediaReactionRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface UpvoteEvent {
        data object LoginRequired : UpvoteEvent
        data class Success(val reactionId: String, val newCount: Int) : UpvoteEvent
        data object Failed : UpvoteEvent
    }

    private data class MediaKey(val mediaId: String, val isAnime: Boolean)

    private val mediaKey = MutableStateFlow<MediaKey?>(null)

    private val upvoteEventChannel = Channel<UpvoteEvent>(Channel.BUFFERED)
    val upvoteEvents: Flow<UpvoteEvent> = upvoteEventChannel.receiveAsFlow()

    fun setMedia(mediaId: String, isAnime: Boolean) {
        val key = MediaKey(mediaId, isAnime)
        if (mediaKey.value != key) {
            mediaKey.value = key
        }
    }

    val dataSource: Flow<PagingData<MediaReaction>> = mediaKey.filterNotNull().flatMapLatest { key ->
        mediaReactionRepository.reactionsPager(key.isAnime, key.mediaId)
    }.cachedIn(viewModelScope)

    fun upvote(reaction: MediaReaction) {
        val userId = getLocalUserId()
        if (userId == null) {
            upvoteEventChannel.trySend(UpvoteEvent.LoginRequired)
            return
        }
        viewModelScope.launch {
            val event = try {
                if (mediaReactionRepository.upvoteReaction(userId, reaction.id)) {
                    UpvoteEvent.Success(reaction.id, reaction.upVotesCount + 1)
                } else {
                    UpvoteEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to upvote reaction with id '${reaction.id}'.", e)
                UpvoteEvent.Failed
            }
            upvoteEventChannel.send(event)
        }
    }

}
