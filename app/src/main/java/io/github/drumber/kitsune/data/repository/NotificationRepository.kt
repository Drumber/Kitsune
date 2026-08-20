package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.NotificationMapper.toNotification
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.source.network.notification.NotificationNetworkDataSource
import io.github.drumber.kitsune.data.source.network.notification.NotificationPagingDataSource
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationRepository(
    private val notificationNetworkDataSource: NotificationNetworkDataSource,
    private val userRepository: UserRepository,
) {

    companion object {
        /**
         * Minimum time in milliseconds between fetching the unseen notifications count.
         */
        private const val NOTIFICATION_UPDATE_INTERVAL = 60_000
    }

    private val notificationFetchMutex = Mutex()
    private var lastNotificationFetch = -1L

    private val _unseenNotificationsCount = MutableStateFlow<Int?>(null)
    val unseenNotificationsCount = _unseenNotificationsCount.asStateFlow()

    suspend fun updateUnseenNotificationsCount(): Unit = notificationFetchMutex.withLock {
        val userId = userRepository.localUser.value?.id ?: return

        if (lastNotificationFetch == -1L || System.currentTimeMillis() - lastNotificationFetch >= NOTIFICATION_UPDATE_INTERVAL) {
            try {
                val count = notificationNetworkDataSource.getUnseenNotificationsCount(userId)
                _unseenNotificationsCount.value = count
            } catch (e: Exception) {
                logE("Error while updating unseen notifications.", e)
            } finally {
                lastNotificationFetch = System.currentTimeMillis()
            }
        }
    }

    suspend fun markAsSeen(userId: String, notifications: List<Notification>) {
        val notificationIds = notifications.map { it.id }
        notificationNetworkDataSource.markNotificationsAsSeen(userId, notificationIds)
        _unseenNotificationsCount.update { count ->
            count?.minus(notifications.size)?.coerceAtLeast(0) ?: 0
        }
    }

    suspend fun markAsRead(userId: String, notifications: List<Notification>) {
        val notificationIds = notifications.map { it.id }
        notificationNetworkDataSource.markNotificationsAsRead(userId, notificationIds)
    }

    fun notificationsPager(userId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            NotificationPagingDataSource { cursor ->
                notificationNetworkDataSource.getNotifications(
                    userId,
                    buildFilter(pageSize, cursor)
                )
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
