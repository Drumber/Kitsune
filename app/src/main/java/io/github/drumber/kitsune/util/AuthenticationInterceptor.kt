package io.github.drumber.kitsune.util

import io.github.drumber.kitsune.data.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response

class AuthenticationInterceptor(private val authRepository: AuthRepository) : Interceptor {

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