package io.github.drumber.kitsune.data.source.network.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkFeedSubject
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.util.logE

/**
 * Cursor based [PagingSource] for the Kitsu activity feed. Each activity group is flattened to
 * the post that is the subject of its most recent activity, matching the behaviour of the
 * official Kitsu apps.
 */
class FeedPagingDataSource(
    private val loadPage: suspend (cursor: String?) -> CursorPageData<NetworkActivityGroup>
) : PagingSource<String, NetworkPost>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, NetworkPost> {
        return try {
            val pageData = loadPage(params.key)

            val posts = pageData.data
                .orEmpty()
                .mapNotNull { group ->
                    group.activities?.firstNotNullOfOrNull { it.subject?.toPostOrNull() }
                }
                .distinctBy { it.id }

            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = pageData.next
            )
        } catch (e: Exception) {
            logE("Error receiving feed data from API.", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, NetworkPost>): String? = null

}

/**
 * Resolves the post represented by a feed activity subject. Post subjects map to themselves,
 * while comment subjects map to the post they were made on.
 */
private fun NetworkFeedSubject.toPostOrNull(): NetworkPost? = when (this) {
    is NetworkPost -> this
    is NetworkComment -> post
    else -> null
}
