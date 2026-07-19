package io.github.drumber.kitsune.data.source.network.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface FollowApi {

    @GET("follows")
    suspend fun getFollows(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkFollow>>

    @POST("follows")
    suspend fun createFollow(
        @Body follow: JSONAPIDocument<NetworkFollow>
    ): JSONAPIDocument<NetworkFollow>

    @DELETE("follows/{id}")
    suspend fun deleteFollow(
        @Path("id") id: String
    ): Response<Unit>

}
