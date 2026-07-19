package io.github.drumber.kitsune.data.source.network.notification

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.util.logE

/**
 * Cursor based [PagingSource] for the Kitsu notifications feed. Each page is a list of
 * activity groups, where every group represents one notification (e.g. someone followed you
 * or liked your post).
 */
class NotificationPagingDataSource(
    private val loadPage: suspend (cursor: String?) -> CursorPageData<NetworkActivityGroup>
) : PagingSource<String, NetworkActivityGroup>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, NetworkActivityGroup> {
        return try {
            val pageData = loadPage(params.key)

            val groups = pageData.data
                .orEmpty()
                .filter { !it.id.isNullOrBlank() }
                .distinctBy { it.id }

            LoadResult.Page(
                data = groups,
                prevKey = null,
                nextKey = pageData.next
            )
        } catch (e: Exception) {
            logE("Error receiving notification data from API.", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, NetworkActivityGroup>): String? = null

}
