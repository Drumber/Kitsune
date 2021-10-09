package io.github.drumber.kitsune.data.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.auth.User
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

}