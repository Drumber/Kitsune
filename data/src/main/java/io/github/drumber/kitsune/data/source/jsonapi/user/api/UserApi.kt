package io.github.drumber.kitsune.data.source.jsonapi.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UserApi {

    @GET("users")
    suspend fun getAllUsers(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkUser>>

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkUser>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: JSONAPIDocument<NetworkUser>
    ): JSONAPIDocument<NetworkUser>

    @DELETE("users/{id}/relationships/waifu")
    suspend fun deleteWaifuRelationship(
        @Path("id") id: String
    ): Response<Unit>

    @GET("users/{id}/profile-links")
    suspend fun getProfileLinksForUser(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkProfileLink>>

}