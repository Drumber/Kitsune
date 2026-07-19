package io.github.drumber.kitsune.ui.notifications

import androidx.paging.PagingData
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.repository.NotificationRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun notificationRepository(): NotificationRepository = mock {
        on { notificationsPager(any(), any()) } doReturn flowOf(PagingData.empty<Notification>())
    }

    @Test
    fun `loginRequired is true when there is no local user`() {
        val getLocalUserId = mock<GetLocalUserIdUseCase> {
            on { invoke() } doReturn null
        }
        val vm = NotificationsViewModel(notificationRepository(), getLocalUserId)
        assertThat(vm.loginRequired).isTrue()
    }

    @Test
    fun `loginRequired is false when a local user is present`() {
        val getLocalUserId = mock<GetLocalUserIdUseCase> {
            on { invoke() } doReturn "user-1"
        }
        val vm = NotificationsViewModel(notificationRepository(), getLocalUserId)
        assertThat(vm.loginRequired).isFalse()
    }

    @Test
    fun `notifications does not request a pager when not logged in`() {
        val repository = notificationRepository()
        val getLocalUserId = mock<GetLocalUserIdUseCase> {
            on { invoke() } doReturn null
        }
        NotificationsViewModel(repository, getLocalUserId)
        verify(repository, never()).notificationsPager(any(), any())
    }

    @Test
    fun `notifications requests a pager for the local user when logged in`() {
        val repository = notificationRepository()
        val getLocalUserId = mock<GetLocalUserIdUseCase> {
            on { invoke() } doReturn "user-1"
        }
        NotificationsViewModel(repository, getLocalUserId)
        verify(repository).notificationsPager(eq("user-1"), any())
    }
}
