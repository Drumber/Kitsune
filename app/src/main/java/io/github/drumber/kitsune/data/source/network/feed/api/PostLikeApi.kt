package io.github.drumber.kitsune.data.source.network.feed.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPostLike
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface PostLikeApi {

    @GET("post-likes")
    suspend fun getPostLikes(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkPostLike>>

    @POST("post-likes")
    suspend fun postPostLike(
        @Body postLike: JSONAPIDocument<NetworkPostLike>
    ): JSONAPIDocument<NetworkPostLike>

    @DELETE("post-likes/{id}")
    suspend fun deletePostLike(
        @Path("id") id: String
    )

}
