package io.github.drumber.kitsune.data.source.network.reaction.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReactionVote
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface MediaReactionApi {

    @GET("media-reactions")
    suspend fun getMediaReactions(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkMediaReaction>>

    @POST("media-reaction-votes")
    suspend fun postMediaReactionVote(
        @Body vote: JSONAPIDocument<NetworkMediaReactionVote>
    ): JSONAPIDocument<NetworkMediaReactionVote>

}
