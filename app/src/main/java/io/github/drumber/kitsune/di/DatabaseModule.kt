package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.source.local.LocalDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { LocalDatabase.createLocalDatabase(androidApplication()) }
    factory { get<LocalDatabase>().libraryEntryDao() }
    factory { get<LocalDatabase>().libraryEntryModificationDao() }
    factory { get<LocalDatabase>().libraryEntryWithModificationDao() }
    factory { get<LocalDatabase>().remoteKeyDao() }
}
