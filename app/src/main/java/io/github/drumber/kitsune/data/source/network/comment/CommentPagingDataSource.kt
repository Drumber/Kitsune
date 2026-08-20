package io.github.drumber.kitsune.data.source.network.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentWithLike
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Offset based [PagingSource] for the top-level comments of a single post. After loading a page of
 * comments, a bounded preview of each comment's replies is fetched in parallel and the current
 * user's likes for every loaded comment (top-level and reply) are resolved in a single request, so
 * that replies and already liked comments are rendered without any further work in the UI layer.
 */
class CommentPagingDataSource(
    private val dataSource: CommentNetworkDataSource,
    private val userId: String?,
    private val filter: Filter,
    private val replyPreviewSize: Int = Kitsu.DEFAULT_REPLY_PREVIEW_SIZE
) : PagingSource<Int, NetworkCommentWithLike>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NetworkCommentWithLike> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val pageData = dataSource.getComments(filter.pageOffset(pageOffset))
            val networkComments = pageData.data.orEmpty()

            // Fetch a bounded reply preview per top-level comment that has replies, in parallel.
            val repliesByParent = coroutineScope {
                networkComments
                    .filter { (it.repliesCount ?: 0) > 0 }
                    .mapNotNull { it.id }
                    .associateWith { parentId ->
                        async { dataSource.getReplies(parentId, replyPreviewSize) }
                    }
                    .mapValues { (_, deferred) -> deferred.await() }
            }

            // Resolve like state for every loaded comment (top-level and preview replies) at once.
            val allComments = networkComments + repliesByParent.values.flatten()
            val likeIdByCommentId = dataSource.resolveLikeIds(userId, allComments.mapNotNull { it.id })

            val comments = networkComments.map { networkComment ->
                networkComment.withReplies(
                    likeIdByCommentId,
                    repliesByParent[networkComment.id].orEmpty()
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

    private fun NetworkComment.withReplies(
        likeIdByCommentId: Map<String, String?>,
        replies: List<NetworkComment>
    ) = NetworkCommentWithLike(
        comment = this,
        likeId = likeIdByCommentId[id],
        replies = replies.map { reply ->
            NetworkCommentWithLike(comment = reply, likeId = likeIdByCommentId[reply.id])
        }
    )

    override fun getRefreshKey(state: PagingState<Int, NetworkCommentWithLike>) =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

}
