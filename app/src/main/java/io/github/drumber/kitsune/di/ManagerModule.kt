package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.GitHubUpdateChecker
import org.koin.dsl.module

val managerModule = module {
    factory { LibraryManager(get(), get()) }
    factory { GitHubUpdateChecker(get()) }
}