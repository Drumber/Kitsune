package io.github.drumber.kitsune.util.network

import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.repository.AuthRepository
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

interface AuthenticationInterceptor : Interceptor, Authenticator

class AuthenticationInterceptorImpl(private val authRepository: AuthRepository) :
    AuthenticationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        if (authRepository.isLoggedIn) {
            authRepository.accessToken?.accessToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
        }
        return chain.proceed(requestBuilder.build())
    }

    /**
     * This method is automatically called by retrofit when a request fails with a 401 code.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.responseCount > 3) return null

        val localAccessTokenHolder = authRepository.accessToken
        val localAccessToken = localAccessTokenHolder?.accessToken
        val localRefreshToken = localAccessTokenHolder?.refreshToken
        if (!authRepository.isLoggedIn || localAccessToken == null || localRefreshToken == null) return null

        // check if the token from the request differs from the local stored access token
        val isTokenAlreadyRefreshed = response.request
            .header("Authorization")
            ?.endsWith(localAccessToken) == false

        val accessToken = if (isTokenAlreadyRefreshed) {
            logD("Local access token was changed during this request. Do not refresh access token and retry with changed access token.")
            localAccessToken
        } else {
            logI("Refreshing access token because of a 401 Unauthorized response.")
            val refreshResponse = runBlocking {
                authRepository.refreshAccessToken(localRefreshToken)
            }
            if (refreshResponse !is Result.Success) return null
            refreshResponse.data.accessToken ?: return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}
