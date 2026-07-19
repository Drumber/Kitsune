package io.github.drumber.kitsune.ui.profile

import app.cash.turbine.test
import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.domain.auth.LogOutUserUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.localUser
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MyProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()

    private fun viewModel(
        userRepository: UserRepository = mock { on { localUser } doReturn MutableStateFlow(null) },
        logOutUser: LogOutUserUseCase = mock()
    ) = MyProfileViewModel(userRepository, logOutUser)

    @Test
    fun `initial ui state is loading`() {
        val vm = viewModel()

        assertThat(vm.uiState.value.isInitialLoading).isTrue()
        assertThat(vm.uiState.value.isRefreshing).isFalse()
    }

    @Test
    fun `getUser returns the mapped local user`() {
        val user = localUser(faker)
        val vm = viewModel(
            userRepository = mock { on { localUser } doReturn MutableStateFlow(user) }
        )

        assertThat(vm.getUser()?.id).isEqualTo(user.id)
    }

    @Test
    fun `getUser returns null when there is no local user`() {
        val vm = viewModel()

        assertThat(vm.getUser()).isNull()
    }

    @Test
    fun `userModel emits the full fetched user and stops initial loading`() = runTest {
        val storedUser = localUser(faker)
        val fullUser = localUser(faker).toUser()
        val userRepository = mock<UserRepository> {
            on { localUser } doReturn MutableStateFlow<LocalUser?>(storedUser)
            onSuspend { fetchUser(eq(storedUser.id), any()) } doReturn fullUser
        }
        val vm = viewModel(userRepository = userRepository)

        vm.userModel.test {
            assertThat(expectMostRecentItem()?.id).isEqualTo(fullUser.id)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.uiState.value.isInitialLoading).isFalse()
        verify(userRepository).fetchUser(eq(storedUser.id), any())
    }

    @Test
    fun `userModel falls back to the local user when the fetch returns null`() = runTest {
        val storedUser = localUser(faker)
        val userRepository = mock<UserRepository> {
            on { localUser } doReturn MutableStateFlow<LocalUser?>(storedUser)
            onSuspend { fetchUser(eq(storedUser.id), any()) } doReturn null
        }
        val vm = viewModel(userRepository = userRepository)

        useMockedAndroidLogger {
            vm.userModel.test {
                assertThat(expectMostRecentItem()?.id).isEqualTo(storedUser.id)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `userModel falls back to the local user when the fetch throws`() = runTest {
        val storedUser = localUser(faker)
        val userRepository = mock<UserRepository> {
            on { localUser } doReturn MutableStateFlow<LocalUser?>(storedUser)
            onSuspend { fetchUser(eq(storedUser.id), any()) } doThrow RuntimeException("boom")
        }
        val vm = viewModel(userRepository = userRepository)

        useMockedAndroidLogger {
            vm.userModel.test {
                assertThat(expectMostRecentItem()?.id).isEqualTo(storedUser.id)
                cancelAndIgnoreRemainingEvents()
            }
        }

        assertThat(vm.uiState.value.isInitialLoading).isFalse()
    }

    @Test
    fun `refreshUser marks the ui state as refreshing`() = runTest {
        val storedUser = localUser(faker)
        val userRepository = mock<UserRepository> {
            on { localUser } doReturn MutableStateFlow<LocalUser?>(storedUser)
            onSuspend { fetchUser(eq(storedUser.id), any()) } doReturn storedUser.toUser()
        }
        val vm = viewModel(userRepository = userRepository)

        vm.uiState.test {
            assertThat(awaitItem().isRefreshing).isFalse()
            vm.refreshUser()
            assertThat(awaitItem().isRefreshing).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logOut delegates to the LogOutUserUseCase`() = runTest {
        val logOutUser = mock<LogOutUserUseCase>()
        val vm = viewModel(logOutUser = logOutUser)

        vm.logOut()

        verify(logOutUser).invoke()
    }
}
