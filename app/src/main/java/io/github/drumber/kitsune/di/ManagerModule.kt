package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.source.network.appupdate.AppReleaseNetworkDataSource
import org.koin.dsl.module

val managerModule = module {
//    factory { LibraryManager(get(), get()) }
//    factory { LibraryEntryDatabaseClient(get()) }
//    factory { LibraryEntryServiceClient(get()) }
    factory { AppReleaseNetworkDataSource(get()) }
}