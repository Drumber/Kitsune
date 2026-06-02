package io.github.drumber.kitsune.data.source.network.feed.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import retrofit2.http.Body
import retrofit2.http.POST

interface PostApi {

    @POST("posts")
    suspend fun postPost(
        @Body post: JSONAPIDocument<NetworkPost>
    ): JSONAPIDocument<NetworkPost>

}
