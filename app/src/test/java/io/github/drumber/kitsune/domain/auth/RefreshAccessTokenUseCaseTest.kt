package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import io.github.drumber.kitsune.testutils.network.FakeHttpException
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.net.UnknownHostException

class RefreshAccessTokenUseCaseTest {

    private val faker = Faker()

    @Test
    fun shouldRefreshAccessTokenWithSuccess(): Unit = runBlocking {
        // given
        val accessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word()
        )

        val accessTokenRepository = mock<AccessTokenRepository> {
            onSuspend { refreshAccessToken() } doReturn accessToken
        }

        val userRepository = mock<UserRepository> {
            onSuspend { promptUserReLogIn() } doReturn Unit
        }

        val logOutUserUseCase = mock<LogOutUserUseCase> {
            doNothing().on { invoke() }
        }

        val refreshAccessToken = RefreshAccessTokenUseCase(accessTokenRepository, userRepository, logOutUserUseCase)

        // when
        val result = useMockedAndroidLogger { refreshAccessToken() }

        // then
        verify(accessTokenRepository).refreshAccessToken()
        verify(logOutUserUseCase, times(0)).invoke()
        assertThat(result).isEqualTo(RefreshResult.Success(accessToken))
    }

    @Test
    fun shouldRefreshAccessTokenWithFailure(): Unit = runBlocking {
        // given
        val accessTokenRepository = mock<AccessTokenRepository> {
            onSuspend { refreshAccessToken() } doThrow FakeHttpException(400)
            doNothing().on { clearAccessToken() }
        }

        val userRepository = mock<UserRepository> {
            onSuspend { promptUserReLogIn() } doReturn Unit
        }

        val logOutUserUseCase = mock<LogOutUserUseCase> {
            doNothing().on { invoke() }
        }

        val refreshAccessToken = RefreshAccessTokenUseCase(accessTokenRepository, userRepository, logOutUserUseCase)

        // when
        val result = useMockedAndroidLogger { refreshAccessToken() }

        // then
        verify(accessTokenRepository).refreshAccessToken()
        verify(userRepository).promptUserReLogIn()
        verify(logOutUserUseCase).invoke()
        assertThat(result).isEqualTo(RefreshResult.Failure)
    }

    @Test
    fun shouldRefreshAccessTokenWithError(): Unit = runBlocking {
        // given
        val accessTokenRepository = mock<AccessTokenRepository> {
            onSuspend { refreshAccessToken() } doAnswer { throw UnknownHostException() }
            doNothing().on { clearAccessToken() }
        }

        val userRepository = mock<UserRepository> {
            onSuspend { promptUserReLogIn() } doReturn Unit
        }

        val logOutUserUseCase = mock<LogOutUserUseCase> {
            doNothing().on { invoke() }
        }

        val refreshAccessToken = RefreshAccessTokenUseCase(accessTokenRepository, userRepository, logOutUserUseCase)

        // when
        val result = useMockedAndroidLogger { refreshAccessToken() }

        // then
        verify(accessTokenRepository).refreshAccessToken()
        verify(logOutUserUseCase, times(0)).invoke()
        assertThat(result).isInstanceOf(RefreshResult.Error::class.java)
    }
}