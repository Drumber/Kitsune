package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.CommentMapper.toComment
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.source.network.comment.CommentNetworkDataSource
import io.github.drumber.kitsune.data.source.network.comment.CommentPagingDataSource
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentLike
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
            pagingData.map { item ->
                item.comment.toComment(
                    isLikedByMe = item.likeId != null,
                    myLikeId = item.likeId
                )
            }
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

    /**
     * Returns the replies of the given top-level comment, oldest first, with the current user's
     * like state resolved. Comment threading is capped at one level by the server.
     */
    suspend fun getReplies(parentCommentId: String, userId: String?): List<Comment> {
        val filter = Filter()
            .filter("parentId", parentCommentId)
            .include("user", "uploads")
            .sort("createdAt")
            .pageLimit(Kitsu.DEFAULT_PAGE_SIZE)
        val networkComments = commentNetworkDataSource.getAllComments(filter)

        val likeIdByCommentId = if (userId != null && networkComments.isNotEmpty()) {
            val commentIds = networkComments.mapNotNull { it.id }
            val likeFilter = Filter()
                .filter("userId", userId)
                .filter("commentId", commentIds.joinToString(","))
                .include("comment")
                .pageLimit(commentIds.size)
            commentNetworkDataSource.getCommentLikes(likeFilter)
                .mapNotNull { like -> like.comment?.id?.let { it to like.id } }
                .toMap()
        } else {
            emptyMap()
        }

        return networkComments.map { networkComment ->
            val likeId = likeIdByCommentId[networkComment.id]
            networkComment.toComment(
                isLikedByMe = likeId != null,
                myLikeId = likeId
            )
        }
    }

    private fun buildCommentFilter(postId: String, pageSize: Int) = Filter()
        .filter("postId", postId)
        .filter("parentId", "_none")
        .include("user", "uploads")
        .sort("createdAt")
        .pageLimit(pageSize)

}
