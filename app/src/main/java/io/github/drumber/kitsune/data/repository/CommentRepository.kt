package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.CommentMapper.toComment
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.source.network.comment.CommentNetworkDataSource
import io.github.drumber.kitsune.data.source.network.comment.CommentPagingDataSource
import io.github.drumber.kitsune.data.source.network.comment.RepliesPagingDataSource
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentLike
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentWithLike
import io.github.drumber.kitsune.data.source.network.comment.resolveLikeIds
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import kotlinx.coroutines.flow.map

class CommentRepository(
    private val commentNetworkDataSource: CommentNetworkDataSource
) {

    /** Pager for the flat list of comments of a single post, oldest first. */
    fun commentsPager(postId: String, userId: String?, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                maxSize = Repository.MAX_CACHED_ITEMS
            ),
            pagingSourceFactory = {
                CommentPagingDataSource(commentNetworkDataSource, userId, buildCommentFilter(postId, pageSize))
            }
        ).flow.map { pagingData ->
            pagingData.map { item -> item.toComment() }
        }

    /** Pager for the full, paginated list of replies of a single parent comment, oldest first. */
    fun repliesPager(parentCommentId: String, userId: String?, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                maxSize = Repository.MAX_CACHED_ITEMS
            ),
            pagingSourceFactory = {
                RepliesPagingDataSource(
                    commentNetworkDataSource,
                    userId,
                    buildRepliesFilter(parentCommentId, pageSize)
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { item -> item.toComment() }
        }

    /** Fetches a single comment by id, including the current user's like state. */
    suspend fun getComment(commentId: String, userId: String?): Comment? {
        val filter = Filter()
            .filter("id", commentId)
            .include("user", "uploads")
            .fields("users", "avatar", "name", "slug", "title")
            .pageLimit(1)
        val networkComment = commentNetworkDataSource.getAllComments(filter).firstOrNull() ?: return null
        val id = networkComment.id ?: return null
        val likeId = commentNetworkDataSource.resolveLikeIds(userId, listOf(id))[id]
        return networkComment.toComment(isLikedByMe = likeId != null, myLikeId = likeId)
    }

    /** Posts a new top-level comment on behalf of the user. Returns the created comment. */
    suspend fun postComment(postId: String, userId: String, content: String): Comment? {
        val comment = NetworkComment(
            id = null,
            content = content,
            post = NetworkPost(id = postId),
            user = NetworkUser(id = userId)
        )
        return commentNetworkDataSource.postComment(comment)?.toComment()
    }

    /**
     * Posts a reply to the given parent comment. Comment threading is capped at one level, so the
     * parent should always be a top-level comment.
     */
    suspend fun postReply(
        postId: String,
        parentCommentId: String,
        userId: String,
        content: String
    ): Comment? {
        val comment = NetworkComment(
            id = null,
            content = content,
            post = NetworkPost(id = postId),
            user = NetworkUser(id = userId),
            parent = NetworkComment(id = parentCommentId)
        )
        return commentNetworkDataSource.postComment(comment)?.toComment()
    }
    suspend fun likeComment(commentId: String, userId: String): String? {
        val like = NetworkCommentLike(
            id = null,
            comment = NetworkComment(id = commentId),
            user = NetworkUser(id = userId)
        )
        return commentNetworkDataSource.postCommentLike(like)?.id
    }

    /** Removes the like with the given id from a comment. */
    suspend fun unlikeComment(likeId: String) {
        commentNetworkDataSource.deleteCommentLike(likeId)
    }

    /** Updates the content of an existing comment owned by the user. Returns the updated comment. */
    suspend fun updateComment(commentId: String, content: String): Comment? {
        val comment = NetworkComment(
            id = commentId,
            content = content
        )
        return commentNetworkDataSource.updateComment(commentId, comment)?.toComment()
    }

    /** Deletes the comment with the given id. */
    suspend fun deleteComment(commentId: String) {
        commentNetworkDataSource.deleteComment(commentId)
    }

    private fun buildCommentFilter(postId: String, pageSize: Int) = Filter()
        .filter("postId", postId)
        .filter("parentId", "_none")
        .include("user", "uploads")
        .fields("users", "avatar", "name", "slug", "title")
        .sort("createdAt")
        .pageLimit(pageSize)

    private fun buildRepliesFilter(parentCommentId: String, pageSize: Int) = Filter()
        .filter("parentId", parentCommentId)
        .include("user", "uploads")
        .fields("users", "avatar", "name", "slug", "title")
        .sort("createdAt")
        .pageLimit(pageSize)

    private fun NetworkCommentWithLike.toComment(): Comment = comment.toComment(
        isLikedByMe = likeId != null,
        myLikeId = likeId
    ).copy(replies = replies.map { it.toComment() })
}
