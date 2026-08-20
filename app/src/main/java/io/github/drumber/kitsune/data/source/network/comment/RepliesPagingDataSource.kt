package io.github.drumber.kitsune.data.source.network.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentWithLike
import io.github.drumber.kitsune.util.logE

/**
 * Offset based [PagingSource] for the full, paginated list of replies of a single parent comment.
 * Like the top-level comment source, it resolves the current user's likes for the loaded replies in
 * a single request. Replies are capped at one level, so they never carry replies of their own.
 */
class RepliesPagingDataSource(
    private val dataSource: CommentNetworkDataSource,
    private val userId: String?,
    private val filter: Filter
) : PagingSource<Int, NetworkCommentWithLike>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NetworkCommentWithLike> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val pageData = dataSource.getComments(filter.pageOffset(pageOffset))
            val networkComments = pageData.data.orEmpty()

            val likeIdByCommentId = dataSource.resolveLikeIds(userId, networkComments.mapNotNull { it.id })

            val replies = networkComments.map { networkComment ->
                NetworkCommentWithLike(
                    comment = networkComment,
                    likeId = likeIdByCommentId[networkComment.id]
                )
            }

            LoadResult.Page(
                data = replies,
                prevKey = pageData.prev,
                nextKey = pageData.next
            )
        } catch (e: Exception) {
            logE("Error receiving replies from API.", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NetworkCommentWithLike>) =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
