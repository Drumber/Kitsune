package io.github.drumber.kitsune.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.repository.NotificationRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    /** IDs of notifications that were already marked as seen. */
    private val seenNotificationIds = mutableSetOf<String>()
    /** Notifications that should be marked as seen after a delay. */
    private val pendingSeenNotifications = mutableSetOf<Notification>()

    private var markAsSeenJob: Job? = null

    /** Whether the user needs to log in to view their notifications. */
    val loginRequired: Boolean
        get() = getLocalUserId() == null

    val notifications: Flow<PagingData<Notification>> = run {
        val userId = getLocalUserId()
        if (userId == null) {
            flowOf(PagingData.empty())
        } else {
            notificationRepository.notificationsPager(userId).cachedIn(viewModelScope)
        }
    }

    fun markNotificationsAsSeen(notifications: List<Notification>) {
        pendingSeenNotifications.addAll(notifications)
        if (markAsSeenJob?.isCompleted ?: true) {
            markAsSeenJob = viewModelScope.launch { flushSeenNotifications() }
        }
    }

    fun markNotificationAsRead(notification: Notification) {
        val userId = getLocalUserId() ?: return
        viewModelScope.launch {
            try {
                // use NonCancellable context to run the request even when navigating to another screen
                withContext(NonCancellable) {
                    notificationRepository.markAsRead(userId, listOf(notification))
                }
            } catch (e: Exception) {
                logE("Failed to mark notification as read.", e)
            }
        }
    }

    private suspend fun flushSeenNotifications() {
        delay(2.seconds)

        val userId = getLocalUserId() ?: return
        val notifications = pendingSeenNotifications.distinctBy { it.id }
        if (notifications.isEmpty()) {
            return
        }

        try {
            notificationRepository.markAsSeen(userId, notifications)
            seenNotificationIds.addAll(notifications.map { it.id })
            pendingSeenNotifications.removeAll(notifications.toSet())
        } catch (e: Exception) {
            logE("Failed to mark notifications as seen.", e)
        }

        if (pendingSeenNotifications.isNotEmpty()) {
            // new notifications have been added in the meantime -> schedule new flush
            flushSeenNotifications()
        }
    }
}
