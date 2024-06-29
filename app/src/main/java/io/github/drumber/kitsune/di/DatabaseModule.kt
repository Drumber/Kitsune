package io.github.drumber.kitsune.di

import android.app.Application
import androidx.room.Room
import io.github.drumber.kitsune.domain_old.database.LocalDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { createLocalDatabase(androidApplication()) }
    factory { get<LocalDatabase>().libraryEntryDao() }
    factory { get<LocalDatabase>().libraryEntryModificationDao() }
    factory { get<LocalDatabase>().libraryEntryWithModification() }
    factory { get<LocalDatabase>().remoteKeyDao() }
}

private fun createLocalDatabase(application: Application): LocalDatabase {
    return Room.databaseBuilder(application, LocalDatabase::class.java, "kitsune.db")
        .fallbackToDestructiveMigration()
        .build()
}
