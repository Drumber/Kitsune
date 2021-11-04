package io.github.drumber.kitsune

import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.di.serviceModule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

class UserServiceTest : AutoCloseKoinTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(serviceModule)
    }

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