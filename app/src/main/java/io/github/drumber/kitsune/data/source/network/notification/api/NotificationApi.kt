package io.github.drumber.kitsune.data.source.network.notification.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface NotificationApi {

    @GET("feeds/notifications/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @POST("feeds/notifications/{userId}/_seen")
    suspend fun markSeen(
        @Path("userId") userId: String,
        @Body notificationIds: List<String>,
    ): JSONAPIDocument<List<NetworkActivityGroup>>

    @POST("feeds/notifications/{userId}/_read")
    suspend fun markRead(
        @Path("userId") userId: String,
        @Body notificationIds: List<String>,
    ): JSONAPIDocument<List<NetworkActivityGroup>>
}
