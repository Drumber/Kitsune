package io.github.drumber.kitsune.domain.user

import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.auth.RefreshAccessTokenIfExpiredUseCase
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UpdateLocalUserUseCaseTest {

    @Test
    fun shouldUpdateLocalUser(): Unit = runBlocking {
        // given
        val userRepository = mock<UserRepository> {
            onSuspend { fetchAndStoreLocalUserFromNetwork() } doReturn Unit
        }

        val isUserLoggedIn = mock<IsUserLoggedInUseCase> {
            on { invoke() } doReturn true
        }

        val refreshAccessTokenIfExpired = mock<RefreshAccessTokenIfExpiredUseCase> {
            onSuspend { invoke() } doReturn null
        }

        val updateLocalUser = UpdateLocalUserUseCase(userRepository, isUserLoggedIn, refreshAccessTokenIfExpired)

        // when
        updateLocalUser()

        // then
        verify(userRepository).fetchAndStoreLocalUserFromNetwork()
    }
}