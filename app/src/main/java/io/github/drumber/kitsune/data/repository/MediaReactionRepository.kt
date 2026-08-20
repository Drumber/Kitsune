package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ReactionMapper.toMediaReaction
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.reaction.ReactionNetworkDataSource
import io.github.drumber.kitsune.data.source.network.reaction.ReactionPagingDataSource
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReactionVote
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import kotlinx.coroutines.flow.map

class MediaReactionRepository(
    private val reactionNetworkDataSource: ReactionNetworkDataSource
) {

    /**
     * Pager for the reactions (short, upvotable reviews) of a single media, sorted by the number
     * of upvotes, matching the reactions section on the media page of the Kitsu website.
     */
    fun reactionsPager(
        isAnime: Boolean,
        mediaId: String,
        pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            ReactionPagingDataSource(reactionNetworkDataSource, buildFilter(isAnime, mediaId, pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toMediaReaction() }
    }

    /**
     * Fetches a limited list of the top reactions for a single media, sorted by upvotes, for the
     * inline preview shown directly on the media page.
     */
    suspend fun getReactions(
        isAnime: Boolean,
        mediaId: String,
        limit: Int = 10
    ): List<MediaReaction> {
        return reactionNetworkDataSource.getMediaReactions(buildFilter(isAnime, mediaId, limit))
            .data
            .orEmpty()
            .map { it.toMediaReaction() }
    }

    /** Fetches a single reaction by id, including the author and the media it belongs to. */
    suspend fun getReaction(reactionId: String): MediaReaction? {
        val filter = Filter()
            .include("user", "anime", "manga")
        return reactionNetworkDataSource.getMediaReaction(reactionId, filter)?.toMediaReaction()
    }

    /** Upvotes the reaction with the given id on behalf of the user. Returns true on success. */
    suspend fun upvoteReaction(userId: String, reactionId: String): Boolean {
        val vote = NetworkMediaReactionVote(
            id = null,
            mediaReaction = NetworkMediaReaction(id = reactionId),
            user = NetworkUser(id = userId)
        )
        return reactionNetworkDataSource.postMediaReactionVote(vote) != null
    }

    /**
     * Posts a new reaction written by the user for the given media. Kitsu requires the media to
     * already be in the user's library, so the matching [libraryEntryId] must be provided.
     * Returns the created reaction on success, or null on failure.
     */
    suspend fun createReaction(
        userId: String,
        libraryEntryId: String,
        isAnime: Boolean,
        mediaId: String,
        reactionText: String
    ): MediaReaction? {
        val reaction = NetworkMediaReaction(
            id = null,
            reaction = reactionText,
            user = NetworkUser(id = userId),
            anime = if (isAnime) NetworkAnime.empty(mediaId) else null,
            manga = if (!isAnime) NetworkManga.empty(mediaId) else null,
            libraryEntry = NetworkLibraryEntry.empty(libraryEntryId)
        )
        return reactionNetworkDataSource.postMediaReaction(reaction)?.toMediaReaction()
    }

    /** Updates the text of an existing reaction. Returns the updated reaction on success. */
    suspend fun updateReaction(reactionId: String, reactionText: String): MediaReaction? {
        val reaction = NetworkMediaReaction(
            id = reactionId,
            reaction = reactionText
        )
        return reactionNetworkDataSource.updateMediaReaction(reactionId, reaction)?.toMediaReaction()
    }

    /** Deletes the reaction with the given id. */
    suspend fun deleteReaction(reactionId: String) {
        reactionNetworkDataSource.deleteMediaReaction(reactionId)
    }

    private fun buildFilter(isAnime: Boolean, mediaId: String, pageSize: Int) = Filter()
        .filter(if (isAnime) "animeId" else "mangaId", mediaId)
        .include("user")
        .sort("-upVotesCount")
        .pageLimit(pageSize)

}
