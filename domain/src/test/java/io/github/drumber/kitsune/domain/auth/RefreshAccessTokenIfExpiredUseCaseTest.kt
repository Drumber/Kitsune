package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import io.github.drumber.kitsune.domain.testutils.useMockedAndroidLogger
import io.github.drumber.kitsune.domain.testutils.onSuspend
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

class RefreshAccessTokenIfExpiredUseCaseTest {

    private val faker = Faker()

    @Test
    fun shouldRefreshAccessTokenIfExpired(): Unit = runBlocking {
        // given
        val localAccessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = (System.currentTimeMillis().milliseconds - 1.days).inWholeSeconds,
            expiresIn = 1.days.inWholeSeconds,
            refreshToken = faker.lorem().word()
        )

        val accessTokenRepository = mock<AccessTokenRepository> {
            on { getAccessToken() } doReturn localAccessToken
            onSuspend { refreshAccessToken() } doReturn localAccessToken
        }

        val refreshAccessTokenUseCase = mock<RefreshAccessTokenUseCase> {
            onSuspend { invoke() } doReturn RefreshResult.Success(localAccessToken)
        }

        val refreshAccessTokenIfExpired =
            RefreshAccessTokenIfExpiredUseCase(accessTokenRepository, refreshAccessTokenUseCase)

        // when
        useMockedAndroidLogger { refreshAccessTokenIfExpired() }

        // then
        verify(accessTokenRepository).getAccessToken()
        verify(refreshAccessTokenUseCase).invoke()
    }

    @Test
    fun shouldNotRefreshAccessTokenIfNotExpired(): Unit = runBlocking {
        // given
        val localAccessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = (System.currentTimeMillis().milliseconds - 1.days).inWholeSeconds,
            expiresIn = 10.days.inWholeSeconds,
            refreshToken = faker.lorem().word()
        )

        val accessTokenRepository = mock<AccessTokenRepository> {
            on { getAccessToken() } doReturn localAccessToken
            onSuspend { refreshAccessToken() } doReturn localAccessToken
        }

        val refreshAccessTokenUseCase = mock<RefreshAccessTokenUseCase> {
            onSuspend { invoke() } doReturn RefreshResult.Success(localAccessToken)
        }

        val refreshAccessTokenIfExpired =
            RefreshAccessTokenIfExpiredUseCase(accessTokenRepository, refreshAccessTokenUseCase)

        // when
        useMockedAndroidLogger { refreshAccessTokenIfExpired() }

        // then
        verify(accessTokenRepository).getAccessToken()
        verify(refreshAccessTokenUseCase, times(0)).invoke()
    }
}