package io.github.drumber.kitsune

import org.junit.Rule
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTestRule

abstract class BaseTest : AutoCloseKoinTest() {

    open val koinModules = emptyList<Module>()

    @get:Rule
    open val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(koinModules)
    }

}