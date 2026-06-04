package io.github.drumber.kitsune.data.source.network.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentWithLike
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
) : PagingSource<Int, NetworkCommentWithLike>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NetworkCommentWithLike> {
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
                NetworkCommentWithLike(
                    comment = networkComment,
                    likeId = likeIdByCommentId[networkComment.id]
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

    override fun getRefreshKey(state: PagingState<Int, NetworkCommentWithLike>) =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

}
