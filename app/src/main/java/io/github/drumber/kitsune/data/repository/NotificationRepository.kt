package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.NotificationMapper.toNotification
import io.github.drumber.kitsune.data.source.network.notification.NotificationNetworkDataSource
import io.github.drumber.kitsune.data.source.network.notification.NotificationPagingDataSource
import kotlinx.coroutines.flow.map

class NotificationRepository(
    private val notificationNetworkDataSource: NotificationNetworkDataSource
) {

    fun notificationsPager(userId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            NotificationPagingDataSource { cursor ->
                notificationNetworkDataSource.getNotifications(userId, buildFilter(pageSize, cursor))
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toNotification() }
    }

    private fun buildFilter(pageSize: Int, cursor: String?) = Filter()
        .include(
            "actor",
            "subject",
            "target.user",
            "target.post",
            "target.anime",
            "target.manga"
        )
        .pageLimit(pageSize)
        .apply { cursor?.let { pageCursor(it) } }

}
