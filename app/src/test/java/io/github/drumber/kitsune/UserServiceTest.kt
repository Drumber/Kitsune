package io.github.drumber.kitsune

import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.di.networkModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserServiceTest : BaseTest() {

    override val koinModules = listOf(networkModule)

    @Test
    fun fetchUser() = runBlocking {
        val userService = getKoin().get<UserService>()
        val filter = Filter().include("stats")
        val response = userService.getUser("1", filter.options)
        val user = response.get()
        assertNotNull(user)
        println("Received user: $user")
        val stats = user?.stats
        assertNotNull(stats)
        println("Included stats: $stats")
    }

}