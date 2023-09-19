package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.domain.manager.GitHubUpdateChecker
import io.github.drumber.kitsune.domain.manager.library.LibraryEntryDatabaseClient
import io.github.drumber.kitsune.domain.manager.library.LibraryEntryServiceClient
import io.github.drumber.kitsune.domain.manager.library.LibraryManager
import org.koin.dsl.module

val managerModule = module {
    factory { LibraryManager(get(), get()) }
    factory { LibraryEntryDatabaseClient(get()) }
    factory { LibraryEntryServiceClient(get()) }
    factory { GitHubUpdateChecker(get()) }
}