package io.github.drumber.kitsune.di

import android.app.Application
import androidx.room.Room
import io.github.drumber.kitsune.domain.database.LocalDatabase
import io.github.drumber.kitsune.domain.room.ResourceDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { createLocalDatabase(androidApplication()) }
    factory { get<LocalDatabase>().libraryEntryDao() }
    factory { get<LocalDatabase>().libraryEntryModificationDao() }
    factory { get<LocalDatabase>().libraryEntryWithModification() }
    factory { get<LocalDatabase>().remoteKeyDao() }
    single { createResourceDatabase(androidApplication()) }
}

private fun createLocalDatabase(application: Application): LocalDatabase {
    return Room.databaseBuilder(application, LocalDatabase::class.java, "kitsune.db")
        .fallbackToDestructiveMigration()
        .build()
}

private fun createResourceDatabase(application: Application): ResourceDatabase {
    return Room.databaseBuilder(application, ResourceDatabase::class.java, "resources.db")
        .build()
}
