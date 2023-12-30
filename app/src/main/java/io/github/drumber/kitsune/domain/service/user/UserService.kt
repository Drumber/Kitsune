package io.github.drumber.kitsune.domain.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.model.infrastructure.user.profilelinks.ProfileLink
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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
    fun deleteWaifuRelationship(
        @Path("id") id: String
    ): Call<ResponseBody>

    @GET("users/{id}/profile-links")
    suspend fun getProfileLinksForUser(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<ProfileLink>>

}