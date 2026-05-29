package io.github.drumber.kitsune.data.source.network.feed.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface FeedApi {

    @GET("feeds/global/global")
    suspend fun getGlobalFeed(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @GET("feeds/timeline/{userId}")
    suspend fun getTimelineFeed(
        @Path("userId") userId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

}
