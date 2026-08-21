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

    @GET("feeds/user_aggr/{userId}")
    suspend fun getUserFeed(
        @Path("userId") userId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @GET("feeds/media_aggr/{feedId}")
    suspend fun getMediaFeed(
        @Path("feedId") feedId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @GET("feeds/episode_aggr/{episodeId}")
    suspend fun getMediaEpisodeFeed(
        @Path("episodeId") episodeId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @GET("feeds/chapter_aggr/{chapterId}")
    suspend fun getMediaChapterFeed(
        @Path("chapterId") chapterId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @GET("feeds/group/{groupId}")
    suspend fun getGroupFeed(
        @Path("groupId") groupId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

}
