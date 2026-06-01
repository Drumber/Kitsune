package io.github.drumber.kitsune.data.source.network.comment.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentLike
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CommentApi {

    @GET("comments")
    suspend fun getComments(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkComment>>

    @POST("comments")
    suspend fun postComment(
        @Body comment: JSONAPIDocument<NetworkComment>
    ): JSONAPIDocument<NetworkComment>

    @GET("comment-likes")
    suspend fun getCommentLikes(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkCommentLike>>

    @POST("comment-likes")
    suspend fun postCommentLike(
        @Body commentLike: JSONAPIDocument<NetworkCommentLike>
    ): JSONAPIDocument<NetworkCommentLike>

    @DELETE("comment-likes/{id}")
    suspend fun deleteCommentLike(
        @Path("id") id: String
    )

}
