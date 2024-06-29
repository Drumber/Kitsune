package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.auth.LocalAccessToken
import io.github.drumber.kitsune.testutils.network.FakeHttpException
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.net.UnknownHostException

class LogInUserUseCaseTest {

    private val faker = Faker()

    @Test
    fun shouldLogInWithSuccess(): Unit = runBlocking {
        // given
        val username = faker.internet().emailAddress()
        val password = faker.internet().password()
        val accessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word()
        )

        val userRepository = mock<UserRepository> {
            onSuspend { updateLocalUserFromNetwork() } doReturn Unit
        }

        val accessTokenRepository = mock<AccessTokenRepository> {
            on { hasAccessToken() } doReturn false
            onSuspend { obtainAccessToken(username, password) } doReturn accessToken
        }

        val isUserLoggedInUserCase = mock<IsUserLoggedInUseCase> {
            on { invoke() } doReturn false
        }

        val logInUser = LogInUserUseCase(
            userRepository,
            accessTokenRepository,
            isUserLoggedInUserCase
        )

        // when
        val result = useMockedAndroidLogger { logInUser(username, password) }

        // then
        verify(accessTokenRepository).obtainAccessToken(username, password)
        verify(userRepository).updateLocalUserFromNetwork()
        verify(isUserLoggedInUserCase).invoke()
        assertThat(result).isEqualTo(LoginResult.Success(accessToken, null))
    }

    @Test
    fun shouldLogInWithFailure(): Unit = runBlocking {
        // given
        val username = faker.internet().emailAddress()
        val password = faker.internet().password()

        val userRepository = mock<UserRepository> {
            onSuspend { updateLocalUserFromNetwork() } doReturn Unit
        }

        val accessTokenRepository = mock<AccessTokenRepository> {
            on { hasAccessToken() } doReturn false
            onSuspend { obtainAccessToken(username, password) } doThrow FakeHttpException(400)
        }

        val isUserLoggedInUserCase = mock<IsUserLoggedInUseCase> {
            on { invoke() } doReturn false
        }

        val logInUser = LogInUserUseCase(
            userRepository,
            accessTokenRepository,
            isUserLoggedInUserCase
        )

        // when
        val result = useMockedAndroidLogger { logInUser(username, password) }

        // then
        verify(accessTokenRepository).obtainAccessToken(username, password)
        verify(userRepository, times(0)).updateLocalUserFromNetwork()
        verify(isUserLoggedInUserCase).invoke()
        assertThat(result).isEqualTo(LoginResult.Failure)
    }

    @Test
    fun shouldLogInWithError(): Unit = runBlocking {
        // given
        val username = faker.internet().emailAddress()
        val password = faker.internet().password()

        val userRepository = mock<UserRepository> {
            onSuspend { updateLocalUserFromNetwork() } doReturn Unit
        }

        val accessTokenRepository = mock<AccessTokenRepository> {
            on { hasAccessToken() } doReturn false
            onSuspend {
                obtainAccessToken(
                    username,
                    password
                )
            } doAnswer { throw UnknownHostException() }
        }

        val isUserLoggedInUserCase = mock<IsUserLoggedInUseCase> {
            on { invoke() } doReturn false
        }

        val logInUser = LogInUserUseCase(
            userRepository,
            accessTokenRepository,
            isUserLoggedInUserCase
        )

        // when
        val result = useMockedAndroidLogger { logInUser(username, password) }

        // then
        verify(accessTokenRepository).obtainAccessToken(username, password)
        verify(userRepository, times(0)).updateLocalUserFromNetwork()
        verify(isUserLoggedInUserCase).invoke()
        assertThat(result).isInstanceOf(LoginResult.Error::class.java)
    }
}