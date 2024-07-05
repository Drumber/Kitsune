package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.domain_old.manager.GitHubUpdateChecker
import org.koin.dsl.module

val managerModule = module {
//    factory { LibraryManager(get(), get()) }
//    factory { LibraryEntryDatabaseClient(get()) }
//    factory { LibraryEntryServiceClient(get()) }
    factory { GitHubUpdateChecker(get()) }
}