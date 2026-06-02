package io.github.drumber.kitsune.data.source.network.notification.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface NotificationApi {

    @GET("feeds/notifications/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkActivityGroup>>

}
