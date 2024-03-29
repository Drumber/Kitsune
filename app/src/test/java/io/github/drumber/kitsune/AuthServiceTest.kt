package io.github.drumber.kitsune

import io.github.drumber.kitsune.di.networkModule
import io.github.drumber.kitsune.domain.service.auth.AuthService
import io.github.drumber.kitsune.testutils.noOpAuthenticatorInterceptor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test

@Ignore
class AuthServiceTest : BaseTest() {

    override val koinModules = listOf(networkModule, noOpAuthenticatorInterceptor)

    @Ignore("Requires working credentials.")
    @Test
    fun obtainAccessToken() = runBlocking {
        val authService = getKoin().get<AuthService>()
        val response = authService.obtainAccessToken(username = "<username/email>", password = "<password>")
        assertNotNull(response)
        println("Access token created at '${response.createdAt}'")
    }

}