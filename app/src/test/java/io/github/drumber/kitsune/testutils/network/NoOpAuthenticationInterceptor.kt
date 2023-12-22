package io.github.drumber.kitsune.testutils.network

import io.github.drumber.kitsune.util.network.AuthenticationInterceptor
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Response

class NoOpAuthenticationInterceptor : AuthenticationInterceptor,
    Authenticator by Authenticator.NONE {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
