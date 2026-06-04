package io.github.drumber.kitsune.data.source.network.reaction.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReactionVote
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MediaReactionApi {

    @GET("media-reactions")
    suspend fun getMediaReactions(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkMediaReaction>>

    @GET("media-reactions/{id}")
    suspend fun getMediaReaction(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkMediaReaction>

    @POST("media-reaction-votes")
    suspend fun postMediaReactionVote(
        @Body vote: JSONAPIDocument<NetworkMediaReactionVote>
    ): JSONAPIDocument<NetworkMediaReactionVote>

    @POST("media-reactions")
    suspend fun postMediaReaction(
        @Body reaction: JSONAPIDocument<NetworkMediaReaction>
    ): JSONAPIDocument<NetworkMediaReaction>

    @PATCH("media-reactions/{id}")
    suspend fun updateMediaReaction(
        @Path("id") id: String,
        @Body reaction: JSONAPIDocument<NetworkMediaReaction>
    ): JSONAPIDocument<NetworkMediaReaction>

    @DELETE("media-reactions/{id}")
    suspend fun deleteMediaReaction(
        @Path("id") id: String
    )

}
