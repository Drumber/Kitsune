package io.github.drumber.kitsune.domain_old.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.User
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks.ProfileLink
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface UserService {

    @GET("users")
    suspend fun allUsers(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<User>>

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<User>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: JSONAPIDocument<User>
    ): JSONAPIDocument<User>

    @DELETE("users/{id}/relationships/waifu")
    suspend fun deleteWaifuRelationship(
        @Path("id") id: String
    ): Response<Unit>

    @GET("users/{id}/profile-links")
    suspend fun getProfileLinksForUser(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<ProfileLink>>

}