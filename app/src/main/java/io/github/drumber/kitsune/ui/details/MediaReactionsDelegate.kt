package io.github.drumber.kitsune.ui.details

import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Encapsulates the media reactions responsibility (loading reactions and upvoting) on behalf of
 * [DetailsViewModel].
 */
class MediaReactionsDelegate(
    private val scope: CoroutineScope,
    private val mediaReactionRepository: MediaReactionRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) {

    private val _reactions = MutableStateFlow<List<MediaReaction>>(emptyList())
    val reactions = _reactions.asStateFlow()

    private val upvoteEventChannel = Channel<ReactionUpvoteEvent>(Channel.BUFFERED)
    val upvoteEvents: Flow<ReactionUpvoteEvent> = upvoteEventChannel.receiveAsFlow()

    private val editEventChannel = Channel<ReactionEditEvent>(Channel.BUFFERED)
    val editEvents: Flow<ReactionEditEvent> = editEventChannel.receiveAsFlow()

    suspend fun loadReactions(media: Media) {
        try {
            val reactions = mediaReactionRepository.getReactions(media is Anime, media.id)
            _reactions.value = reactions
        } catch (e: Exception) {
            logE("Failed to load reactions for media '${media.id}'.", e)
        }
    }

    fun upvoteReaction(reaction: MediaReaction) {
        val userId = getLocalUserId()
        if (userId == null) {
            upvoteEventChannel.trySend(ReactionUpvoteEvent.LoginRequired)
            return
        }
        scope.launch {
            val event = try {
                if (mediaReactionRepository.upvoteReaction(userId, reaction.id)) {
                    ReactionUpvoteEvent.Success(reaction.id, reaction.upVotesCount + 1)
                } else {
                    ReactionUpvoteEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to upvote reaction with id '${reaction.id}'.", e)
                ReactionUpvoteEvent.Failed
            }
            upvoteEventChannel.send(event)
        }
    }

    /**
     * Posts a new reaction for the given media. Kitsu requires the media to already be in the
     * user's library, so the matching [libraryEntryId] must be provided; if it is null the
     * [ReactionEditEvent.AddToLibraryRequired] event is emitted instead. The reactions list is
     * reloaded on success so the new reaction shows up in the preview.
     */
    fun createReaction(media: Media, libraryEntryId: String?, text: String) {
        val userId = getLocalUserId()
        if (userId == null) {
            editEventChannel.trySend(ReactionEditEvent.LoginRequired)
            return
        }
        val reactionText = text.trim()
        if (reactionText.isEmpty()) return
        if (libraryEntryId == null) {
            editEventChannel.trySend(ReactionEditEvent.AddToLibraryRequired)
            return
        }
        scope.launch {
            val event = try {
                val created = mediaReactionRepository.createReaction(
                    userId = userId,
                    libraryEntryId = libraryEntryId,
                    isAnime = media is Anime,
                    mediaId = media.id,
                    reactionText = reactionText
                )
                if (created != null) {
                    loadReactions(media)
                    ReactionEditEvent.Created
                } else {
                    ReactionEditEvent.Failed
                }
            } catch (e: Exception) {
                logE("Failed to create reaction for media '${media.id}'.", e)
                ReactionEditEvent.Failed
            }
            editEventChannel.send(event)
        }
    }
}
