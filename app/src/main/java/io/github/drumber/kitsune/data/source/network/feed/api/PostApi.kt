package io.github.drumber.kitsune.data.source.network.feed.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PostApi {

    @POST("posts")
    suspend fun postPost(
        @Body post: JSONAPIDocument<NetworkPost>
    ): JSONAPIDocument<NetworkPost>

    @PATCH("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body post: JSONAPIDocument<NetworkPost>
    ): JSONAPIDocument<NetworkPost>

    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Path("id") id: String
    ): Response<Unit>

}
