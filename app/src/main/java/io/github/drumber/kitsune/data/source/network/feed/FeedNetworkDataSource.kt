package io.github.drumber.kitsune.data.source.network.feed

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.feed.api.FeedApi
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.toCursorPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedNetworkDataSource(
    private val feedApi: FeedApi
) {

    suspend fun getGlobalFeed(filter: Filter): CursorPageData<NetworkActivityGroup> {
        return withContext(Dispatchers.IO) {
            feedApi.getGlobalFeed(filter.options).toCursorPageData()
        }
    }

    suspend fun getTimelineFeed(userId: String, filter: Filter): CursorPageData<NetworkActivityGroup> {
        return withContext(Dispatchers.IO) {
            feedApi.getTimelineFeed(userId, filter.options).toCursorPageData()
        }
    }

}
