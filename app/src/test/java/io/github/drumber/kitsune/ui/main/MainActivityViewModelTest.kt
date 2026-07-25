package io.github.drumber.kitsune.ui.main

import app.cash.turbine.test
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.AccessTokenRepository.AccessTokenState
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun userRepository(): UserRepository = mock {
        on { userReLogInPrompt } doReturn MutableSharedFlow<Unit>().asSharedFlow()
        on { localUser } doReturn MutableStateFlow(null).asStateFlow()
    }

    private fun accessTokenRepository(state: AccessTokenState): AccessTokenRepository = mock {
        on { accessTokenState } doReturn MutableStateFlow(state).asStateFlow()
    }

    private fun viewModel(
        userRepository: UserRepository = userRepository(),
        accessTokenRepository: AccessTokenRepository = accessTokenRepository(AccessTokenState.NOT_PRESENT)
    ) = MainActivityViewModel(userRepository, accessTokenRepository)

    @Test
    fun `isLoggedIn returns true when access token is present`() {
        val vm = viewModel(
            accessTokenRepository = accessTokenRepository(AccessTokenState.PRESENT)
        )
        assertThat(vm.isLoggedIn()).isTrue()
    }

    @Test
    fun `isLoggedIn returns false when access token is not present`() {
        val vm = viewModel(
            accessTokenRepository = accessTokenRepository(AccessTokenState.NOT_PRESENT)
        )
        assertThat(vm.isLoggedIn()).isFalse()
    }

    @Test
    fun `isLoggedInFlow emits true when access token is present`() = runTest {
        val vm = viewModel(
            accessTokenRepository = accessTokenRepository(AccessTokenState.PRESENT)
        )
        vm.isLoggedInFlow.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `isLoggedInFlow emits false when access token is not present`() = runTest {
        val vm = viewModel(
            accessTokenRepository = accessTokenRepository(AccessTokenState.NOT_PRESENT)
        )
        vm.isLoggedInFlow.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `reLoginPrompt and localUser delegate to user repository`() {
        val reLoginFlow = MutableSharedFlow<Unit>().asSharedFlow()
        val localUserFlow = MutableStateFlow(null).asStateFlow()
        val userRepository = mock<UserRepository> {
            on { userReLogInPrompt } doReturn reLoginFlow
            on { localUser } doReturn localUserFlow
        }
        val vm = viewModel(userRepository = userRepository)
        assertThat(vm.reLoginPrompt).isSameAs(reLoginFlow)
        assertThat(vm.localUser).isSameAs(localUserFlow)
    }
}
