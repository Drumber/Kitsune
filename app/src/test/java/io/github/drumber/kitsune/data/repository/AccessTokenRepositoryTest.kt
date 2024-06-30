package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.source.local.auth.AccessTokenLocalDataSource
import io.github.drumber.kitsune.data.source.local.auth.LocalAccessToken
import io.github.drumber.kitsune.data.source.network.auth.AccessTokenNetworkDataSource
import io.github.drumber.kitsune.data.source.network.auth.model.NetworkAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.ObtainAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.RefreshAccessToken
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class AccessTokenRepositoryTest {

    private val faker = Faker()

    @Test
    fun shouldGetAccessToken() {
        // given
        val accessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word()
        )

        val localAccessTokenDataSource = mock<AccessTokenLocalDataSource> {
            on { loadAccessToken() } doReturn accessToken
        }

        val accessTokenRepository = AccessTokenRepository(
            localAccessTokenDataSource = localAccessTokenDataSource,
            remoteAccessTokenDataSource = mock(stubOnly = true)
        )

        // when
        val actualAccessToken = accessTokenRepository.getAccessToken()

        // then
        verify(localAccessTokenDataSource).loadAccessToken()
        assertThat(actualAccessToken).isEqualTo(accessToken)
    }

    @Test
    fun shouldClearAccessToken() {
        // given
        val localAccessTokenDataSource = mock<AccessTokenLocalDataSource> {
            doNothing().on { clearAccessToken() }
        }

        val accessTokenRepository = AccessTokenRepository(
            localAccessTokenDataSource = localAccessTokenDataSource,
            remoteAccessTokenDataSource = mock(stubOnly = true)
        )

        // when
        accessTokenRepository.clearAccessToken()

        // then
        verify(localAccessTokenDataSource).clearAccessToken()
    }

    @Test
    fun shouldObtainAccessToken(): Unit = runBlocking {
        // given
        val username = faker.name().username()
        val password = faker.lorem().word()
        val accessToken = NetworkAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word(),
            tokenType = "bearer",
            scope = "public"
        )

        val localAccessTokenDataSource = mock<AccessTokenLocalDataSource> {
            doNothing().on { storeAccessToken(any()) }
        }
        val remoteAccessTokenDataSource = mock<AccessTokenNetworkDataSource> {
            onSuspend { obtainAccessToken(any()) } doReturn accessToken
        }

        val accessTokenRepository = AccessTokenRepository(
            localAccessTokenDataSource = localAccessTokenDataSource,
            remoteAccessTokenDataSource = remoteAccessTokenDataSource
        )

        // when
        val actualAccessToken = accessTokenRepository.obtainAccessToken(username, password)

        // then
        val obtainAccessTokenCaptor = argumentCaptor<ObtainAccessToken>()
        verify(remoteAccessTokenDataSource).obtainAccessToken(obtainAccessTokenCaptor.capture())
        assertThat(obtainAccessTokenCaptor.firstValue.username).isEqualTo(username)
        assertThat(obtainAccessTokenCaptor.firstValue.password).isEqualTo(password)

        val storeAccessTokenCaptor = argumentCaptor<LocalAccessToken>()
        verify(localAccessTokenDataSource).storeAccessToken(storeAccessTokenCaptor.capture())
        assertThat(storeAccessTokenCaptor.firstValue)
            .usingRecursiveComparison()
            .isEqualTo(accessToken)

        assertThat(actualAccessToken).usingRecursiveComparison().isEqualTo(accessToken)
    }

    @Test
    fun shouldRefreshAccessToken(): Unit = runBlocking {
        // given
        val localAccessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word()
        )
        val networkAccessToken = NetworkAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word(),
            tokenType = "bearer",
            scope = "public"
        )

        val localAccessTokenDataSource = mock<AccessTokenLocalDataSource> {
            on { loadAccessToken() } doReturn localAccessToken
            doNothing().on { storeAccessToken(any()) }
        }
        val remoteAccessTokenDataSource = mock<AccessTokenNetworkDataSource> {
            onSuspend { refreshToken(any()) } doReturn networkAccessToken
        }

        val accessTokenRepository = AccessTokenRepository(
            localAccessTokenDataSource = localAccessTokenDataSource,
            remoteAccessTokenDataSource = remoteAccessTokenDataSource
        )

        // when
        val actualAccessToken = accessTokenRepository.refreshAccessToken()

        // then
        val refreshAccessTokenCaptor = argumentCaptor<RefreshAccessToken>()
        verify(remoteAccessTokenDataSource).refreshToken(refreshAccessTokenCaptor.capture())
        assertThat(refreshAccessTokenCaptor.firstValue.refreshToken).isEqualTo(localAccessToken.refreshToken)

        val storeAccessTokenCaptor = argumentCaptor<LocalAccessToken>()
        verify(localAccessTokenDataSource).storeAccessToken(storeAccessTokenCaptor.capture())
        assertThat(storeAccessTokenCaptor.firstValue)
            .usingRecursiveComparison()
            .isEqualTo(networkAccessToken)

        assertThat(actualAccessToken).usingRecursiveComparison().isEqualTo(networkAccessToken)
    }

    @Test
    fun shouldRefreshAccessTokenOnlyOnce(): Unit = runBlocking {
        // given
        val localAccessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.internet().uuid()
        )
        val networkAccessToken ={
            NetworkAccessToken(
                accessToken = faker.lorem().word(),
                createdAt = faker.number().randomNumber(),
                expiresIn = faker.number().randomNumber(),
                refreshToken = faker.internet().uuid(),
                tokenType = "bearer",
                scope = "public"
            )
        }

        val localAccessTokenDataSource = FakeAccessTokenLocalDataSource(localAccessToken)
        val remoteAccessTokenDataSource = mock<AccessTokenNetworkDataSource> {
            onSuspend { refreshToken(any()) } doAnswer {
                runBlocking {
                    delay(10) // simulate network delay
                    networkAccessToken()
                }
            }
        }

        val accessTokenRepository = AccessTokenRepository(
            localAccessTokenDataSource = localAccessTokenDataSource,
            remoteAccessTokenDataSource = remoteAccessTokenDataSource
        )

        useMockedAndroidLogger {
            // when
            val firstAccessToken = async { accessTokenRepository.refreshAccessToken() }
            val secondAccessToken = async { accessTokenRepository.refreshAccessToken() }

            // then
            awaitAll(firstAccessToken, secondAccessToken)
            verify(remoteAccessTokenDataSource, times(1)).refreshToken(any())
            assertThat(firstAccessToken.await()).isEqualTo(localAccessTokenDataSource.loadAccessToken())
            assertThat(firstAccessToken.await()).isEqualTo(secondAccessToken.await())
        }
    }


    class FakeAccessTokenLocalDataSource(
        private var accessToken: LocalAccessToken? = null
    ) : AccessTokenLocalDataSource {

        override fun loadAccessToken(): LocalAccessToken? {
            return accessToken
        }

        override fun storeAccessToken(accessToken: LocalAccessToken) {
            this.accessToken = accessToken
        }

        override fun clearAccessToken() {
            accessToken = null
        }
    }
}