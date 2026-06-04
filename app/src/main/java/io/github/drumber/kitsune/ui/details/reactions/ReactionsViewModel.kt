package io.github.drumber.kitsune.ui.details.reactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.LibraryRepository
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
    private val getLocalUserId: GetLocalUserIdUseCase,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    sealed interface UpvoteEvent {
        data object LoginRequired : UpvoteEvent
        data class Success(val reactionId: String, val newCount: Int) : UpvoteEvent
        data object Failed : UpvoteEvent
    }

    sealed interface EditEvent {
        data object LoginRequired : EditEvent
        data object AddToLibraryRequired : EditEvent
        data object Created : EditEvent
        data object Updated : EditEvent
        data object Deleted : EditEvent
        data object Failed : EditEvent
    }

    private data class MediaKey(val mediaId: String, val isAnime: Boolean)

    private val mediaKey = MutableStateFlow<MediaKey?>(null)

    private val upvoteEventChannel = Channel<UpvoteEvent>(Channel.BUFFERED)
    val upvoteEvents: Flow<UpvoteEvent> = upvoteEventChannel.receiveAsFlow()

    private val editEventChannel = Channel<EditEvent>(Channel.BUFFERED)
    val editEvents: Flow<EditEvent> = editEventChannel.receiveAsFlow()

    /** Id of the currently logged-in user, used to identify the user's own reactions. */
    val currentUserId: String?
        get() = getLocalUserId()

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

    fun createReaction(text: String) {
        val key = mediaKey.value ?: return
        val userId = getLocalUserId()
        if (userId == null) {
            editEventChannel.trySend(EditEvent.LoginRequired)
            return
        }
        val reactionText = text.trim()
        if (reactionText.isEmpty()) return
        viewModelScope.launch {
            val event = try {
                val libraryEntryId = findLibraryEntryId(userId, key)
                when {
                    libraryEntryId == null -> EditEvent.AddToLibraryRequired
                    mediaReactionRepository.createReaction(
                        userId = userId,
                        libraryEntryId = libraryEntryId,
                        isAnime = key.isAnime,
                        mediaId = key.mediaId,
                        reactionText = reactionText
                    ) != null -> EditEvent.Created

                    else -> EditEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to create reaction for media '${key.mediaId}'.", e)
                EditEvent.Failed
            }
            editEventChannel.send(event)
        }
    }

    fun updateReaction(reaction: MediaReaction, text: String) {
        if (getLocalUserId() == null) {
            editEventChannel.trySend(EditEvent.LoginRequired)
            return
        }
        val reactionText = text.trim()
        if (reactionText.isEmpty()) return
        viewModelScope.launch {
            val event = try {
                if (mediaReactionRepository.updateReaction(reaction.id, reactionText) != null) {
                    EditEvent.Updated
                } else {
                    EditEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to update reaction with id '${reaction.id}'.", e)
                EditEvent.Failed
            }
            editEventChannel.send(event)
        }
    }

    fun deleteReaction(reaction: MediaReaction) {
        if (getLocalUserId() == null) {
            editEventChannel.trySend(EditEvent.LoginRequired)
            return
        }
        viewModelScope.launch {
            val event = try {
                mediaReactionRepository.deleteReaction(reaction.id)
                EditEvent.Deleted
            } catch (e: Exception) {
                logE("Failed to delete reaction with id '${reaction.id}'.", e)
                EditEvent.Failed
            }
            editEventChannel.send(event)
        }
    }

    private suspend fun findLibraryEntryId(userId: String, key: MediaKey): String? {
        val filter = Filter()
            .filter("user_id", userId)
            .filter(if (key.isAnime) "anime_id" else "manga_id", key.mediaId)
            .fields("libraryEntries", "status")
            .pageLimit(1)
        return libraryRepository.fetchAllLibraryEntries(filter)?.firstOrNull()?.id
    }

}
