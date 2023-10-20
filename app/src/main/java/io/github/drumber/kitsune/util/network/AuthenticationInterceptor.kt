package io.github.drumber.kitsune.util.network

import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.repository.AuthRepository
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

        val refreshToken = authRepository.accessToken?.refreshToken
        if (!authRepository.isLoggedIn || refreshToken == null) return null

        val refreshResponse = runBlocking { authRepository.refreshAccessToken(refreshToken) }
        if (refreshResponse !is Result.Success) return null

        val accessToken = refreshResponse.data.accessToken ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }

    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()
}

/**
 * Dummy implementation **only for test cases**.
 */
class AuthenticationInterceptorDummy : AuthenticationInterceptor,
    Authenticator by Authenticator.NONE {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}