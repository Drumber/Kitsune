package io.github.drumber.kitsune.util.network

import io.github.drumber.kitsune.domain.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response

interface AuthenticationInterceptor : Interceptor

class AuthenticationInterceptorImpl(private val authRepository: AuthRepository) :
    AuthenticationInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        if (authRepository.isLoggedIn) {
            authRepository.accessToken?.accessToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}

/**
 * Dummy implementation **only for test cases**.
 */
class AuthenticationInterceptorDummy : AuthenticationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}