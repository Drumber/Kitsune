package io.github.drumber.kitsune.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class FeedViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    val unseenNotificationsCount = notificationRepository.unseenNotificationsCount

    fun updateUnseenNotificationsCount() {
        viewModelScope.launch {
            notificationRepository.updateUnseenNotificationsCount()
        }
    }
}
