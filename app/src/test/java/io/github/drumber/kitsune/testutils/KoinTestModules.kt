package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.testutils.network.NoOpAuthenticationInterceptor
import io.github.drumber.kitsune.util.network.AuthenticationInterceptor
import org.koin.dsl.module

val noOpAuthenticatorInterceptor = module {
    factory<AuthenticationInterceptor> { NoOpAuthenticationInterceptor() }
}
