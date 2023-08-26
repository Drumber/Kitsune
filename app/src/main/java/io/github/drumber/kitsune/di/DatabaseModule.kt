package io.github.drumber.kitsune.di

import android.app.Application
import androidx.room.Room
import io.github.drumber.kitsune.domain.room.ResourceDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { createResourceDatabase(androidApplication()) }
    single { get<ResourceDatabase>().libraryEntryDao() }
    single { get<ResourceDatabase>().offlineLibraryModificationDao() }
}

private fun createResourceDatabase(application: Application): ResourceDatabase {
    // TODO: add migration for offline library modification
    return Room.databaseBuilder(application, ResourceDatabase::class.java, "resources.db")
        .fallbackToDestructiveMigration()
        .build()
}
