package io.github.drumber.kitsune.data.source.network.notification

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.notification.api.NotificationApi
import io.github.drumber.kitsune.data.source.network.toCursorPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationNetworkDataSource(
    private val notificationApi: NotificationApi
) {

    suspend fun getNotifications(
        userId: String,
        filter: Filter
    ): CursorPageData<NetworkActivityGroup> {
        return withContext(Dispatchers.IO) {
            notificationApi.getNotifications(userId, filter.options).toCursorPageData()
        }
    }

}
