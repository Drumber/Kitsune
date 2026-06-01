package io.github.drumber.kitsune.data.source.network.comment

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.comment.api.CommentApi
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentLike
import io.github.drumber.kitsune.data.source.network.toPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommentNetworkDataSource(
    private val commentApi: CommentApi
) {

    suspend fun getComments(filter: Filter): PageData<NetworkComment> {
        return withContext(Dispatchers.IO) {
            commentApi.getComments(filter.options).toPageData()
        }
    }

    suspend fun getAllComments(filter: Filter): List<NetworkComment> {
        return withContext(Dispatchers.IO) {
            commentApi.getComments(filter.options).get().orEmpty()
        }
    }

    suspend fun postComment(comment: NetworkComment): NetworkComment? {
        return withContext(Dispatchers.IO) {
            commentApi.postComment(JSONAPIDocument(comment)).get()
        }
    }

    suspend fun getCommentLikes(filter: Filter): List<NetworkCommentLike> {
        return withContext(Dispatchers.IO) {
            commentApi.getCommentLikes(filter.options).get().orEmpty()
        }
    }

    suspend fun postCommentLike(commentLike: NetworkCommentLike): NetworkCommentLike? {
        return withContext(Dispatchers.IO) {
            commentApi.postCommentLike(JSONAPIDocument(commentLike)).get()
        }
    }

    suspend fun deleteCommentLike(id: String) {
        withContext(Dispatchers.IO) {
            commentApi.deleteCommentLike(id)
        }
    }

}
