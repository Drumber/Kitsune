package io.github.drumber.kitsune.util.network

import io.github.drumber.kitsune.config.Kitsu.API_HOST
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.domain.auth.RefreshAccessTokenUseCase
import io.github.drumber.kitsune.domain.auth.RefreshResult
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface AuthenticationInterceptor : Interceptor, Authenticator

class AuthenticationInterceptorImpl(
    private val accessTokenRepository: AccessTokenRepository
) : AuthenticationInterceptor, KoinComponent {

    /** Ensures only one token refresh runs at a time across concurrent 401 responses. */
    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        if (chain.request().url.host == API_HOST) {
            accessTokenRepository.getAccessToken()?.accessToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
        }
        return chain.proceed(requestBuilder.build())
    }

    /**
     * This method is automatically called by retrofit when a request fails with a 401 code.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.host != API_HOST || response.responseCount > 3) return null

        val localAccessTokenHolder = accessTokenRepository.getAccessToken()
        val localAccessToken = localAccessTokenHolder?.accessToken
        if (!accessTokenRepository.hasAccessToken() || localAccessToken == null) return null

        // check if the token from the request differs from the local stored access token
        val isTokenAlreadyRefreshed = response.request
            .header("Authorization")
            ?.endsWith(localAccessToken) == false

        val accessToken = if (isTokenAlreadyRefreshed) {
            logD("Local access token was changed during this request. Do not refresh access token and retry with changed access token.")
            localAccessToken
        } else {
            logI("Refreshing access token because of a 401 Unauthorized response.")
            val refreshedToken = runBlocking {
                refreshMutex.withLock {
                    // A concurrent 401 may have already refreshed the token while we waited for
                    // the lock. In that case reuse it instead of refreshing again.
                    val currentToken = accessTokenRepository.getAccessToken()?.accessToken
                    if (currentToken != null && currentToken != localAccessToken) {
                        logD("Access token was already refreshed by a concurrent request. Reusing it.")
                        currentToken
                    } else {
                        try {
                            withTimeout(REFRESH_TIMEOUT_MS) {
                                val refreshAccessToken: RefreshAccessTokenUseCase = get()
                                refreshAccessToken()
                            }.let { (it as? RefreshResult.Success)?.accessToken?.accessToken }
                        } catch (e: TimeoutCancellationException) {
                            logE("Refreshing access token timed out after ${REFRESH_TIMEOUT_MS}ms.", e)
                            null
                        }
                    }
                }
            } ?: return null
            refreshedToken
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()

    companion object {
        private const val REFRESH_TIMEOUT_MS = 30_000L
    }
}
