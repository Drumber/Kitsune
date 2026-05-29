package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ReactionMapper.toMediaReaction
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

    /** Upvotes the reaction with the given id on behalf of the user. Returns true on success. */
    suspend fun upvoteReaction(userId: String, reactionId: String): Boolean {
        val vote = NetworkMediaReactionVote(
            id = null,
            mediaReaction = NetworkMediaReaction(id = reactionId),
            user = NetworkUser(id = userId)
        )
        return reactionNetworkDataSource.postMediaReactionVote(vote) != null
    }

    private fun buildFilter(isAnime: Boolean, mediaId: String, pageSize: Int) = Filter()
        .filter(if (isAnime) "animeId" else "mangaId", mediaId)
        .include("user")
        .sort("-upVotesCount")
        .pageLimit(pageSize)

}
