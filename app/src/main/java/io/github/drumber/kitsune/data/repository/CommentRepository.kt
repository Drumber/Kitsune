package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
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
        ).flow

    /** Returns up to [limit] distinct commenter avatar urls for the given post, oldest first. */
    suspend fun getTopCommenterAvatars(postId: String, limit: Int = 3): List<String> {
        val filter = Filter()
            .filter("postId", postId)
            .include("user")
            .sort("createdAt")
            .pageLimit(limit * 4)
        return commentNetworkDataSource.getAllComments(filter)
            .mapNotNull { it.user }
            .distinctBy { it.id }
            .mapNotNull { it.avatar?.originalOrDown() }
            .take(limit)
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

    /** Likes the given comment on behalf of the user. Returns the created like id, or null on failure. */
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

    private fun buildCommentFilter(postId: String, pageSize: Int) = Filter()
        .filter("postId", postId)
        .include("user", "uploads")
        .sort("createdAt")
        .pageLimit(pageSize)

}
