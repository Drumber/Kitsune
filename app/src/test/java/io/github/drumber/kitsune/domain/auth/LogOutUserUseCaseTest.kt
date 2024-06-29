package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import org.junit.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LogOutUserUseCaseTest {

    @Test
    fun shouldLogOut() {
        // given
        val userRepository = mock<UserRepository> {
            doNothing().on { clearLocalUser() }
        }

        val accessTokenRepository = mock<AccessTokenRepository> {
            doNothing().on { clearAccessToken() }
        }

        val logOutUser = LogOutUserUseCase(userRepository, accessTokenRepository)

        // when
        useMockedAndroidLogger { logOutUser() }

        // then
        verify(userRepository).clearLocalUser()
        verify(accessTokenRepository).clearAccessToken()
    }
}