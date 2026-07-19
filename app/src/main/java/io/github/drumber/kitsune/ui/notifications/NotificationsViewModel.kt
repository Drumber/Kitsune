package io.github.drumber.kitsune.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.repository.NotificationRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

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

}
