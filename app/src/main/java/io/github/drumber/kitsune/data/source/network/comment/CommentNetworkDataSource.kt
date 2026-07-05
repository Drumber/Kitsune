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

    /** Fetches up to [limit] replies of the given parent comment, oldest first. */
    suspend fun getReplies(parentCommentId: String, limit: Int): List<NetworkComment> {
        val filter = Filter()
            .filter("parentId", parentCommentId)
            .include("user", "uploads")
            .sort("createdAt")
            .pageLimit(limit)
        return getAllComments(filter)
    }

    suspend fun postComment(comment: NetworkComment): NetworkComment? {
        return withContext(Dispatchers.IO) {
            commentApi.postComment(JSONAPIDocument(comment)).get()
        }
    }

    suspend fun updateComment(id: String, comment: NetworkComment): NetworkComment? {
        return withContext(Dispatchers.IO) {
            commentApi.updateComment(id, JSONAPIDocument(comment)).get()
        }
    }

    suspend fun deleteComment(id: String) {
        withContext(Dispatchers.IO) {
            commentApi.deleteComment(id)
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

/**
 * Resolves the current user's like ids for the given comments in a single request, keyed by comment
 * id. Returns an empty map when [userId] is `null` or [commentIds] is empty, avoiding a needless
 * network call. Shared by the comment and reply paging sources.
 */
internal suspend fun CommentNetworkDataSource.resolveLikeIds(
    userId: String?,
    commentIds: List<String>
): Map<String, String?> {
    if (userId == null || commentIds.isEmpty()) return emptyMap()
    val likeFilter = Filter()
        .filter("userId", userId)
        .filter("commentId", commentIds.joinToString(","))
        .include("comment")
        .pageLimit(commentIds.size)
    return getCommentLikes(likeFilter)
        .mapNotNull { like -> like.comment?.id?.let { it to like.id } }
        .toMap()
}
