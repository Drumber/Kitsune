package io.github.drumber.kitsune.data.source.network.reaction

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.reaction.api.MediaReactionApi
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReactionVote
import io.github.drumber.kitsune.data.source.network.toPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReactionNetworkDataSource(
    private val mediaReactionApi: MediaReactionApi
) {

    suspend fun getMediaReactions(filter: Filter): PageData<NetworkMediaReaction> {
        return withContext(Dispatchers.IO) {
            mediaReactionApi.getMediaReactions(filter.options).toPageData()
        }
    }

    suspend fun postMediaReactionVote(vote: NetworkMediaReactionVote): NetworkMediaReactionVote? {
        return withContext(Dispatchers.IO) {
            mediaReactionApi.postMediaReactionVote(JSONAPIDocument(vote)).get()
        }
    }

}
