package io.github.drumber.kitsune.data.source.network.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.CommentMapper.toComment
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.util.logE

/**
 * Offset based [PagingSource] for the top-level comments of a single post. After loading a page
 * of comments, the current user's likes for those comments are fetched in a single request so
 * that already liked comments are pre-marked in the UI.
 */
class CommentPagingDataSource(
    private val dataSource: CommentNetworkDataSource,
    private val userId: String?,
    private val filter: Filter
) : PagingSource<Int, Comment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val pageData = dataSource.getComments(filter.pageOffset(pageOffset))
            val networkComments = pageData.data.orEmpty()

            val likeIdByCommentId = if (userId != null && networkComments.isNotEmpty()) {
                val commentIds = networkComments.mapNotNull { it.id }
                val likeFilter = Filter()
                    .filter("userId", userId)
                    .filter("commentId", commentIds.joinToString(","))
                    .include("comment")
                    .pageLimit(commentIds.size)
                dataSource.getCommentLikes(likeFilter)
                    .mapNotNull { like -> like.comment?.id?.let { it to like.id } }
                    .toMap()
            } else {
                emptyMap()
            }

            val comments = networkComments.map { networkComment ->
                val likeId = likeIdByCommentId[networkComment.id]
                networkComment.toComment(
                    isLikedByMe = likeId != null,
                    myLikeId = likeId
                )
            }

            LoadResult.Page(
                data = comments,
                prevKey = pageData.prev,
                nextKey = pageData.next
            )
        } catch (e: Exception) {
            logE("Error receiving comments from API.", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Comment>) =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

}
