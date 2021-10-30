package io.github.drumber.kitsune.di

import android.app.Application
import androidx.room.Room
import io.github.drumber.kitsune.data.room.ResourceDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { createResourceDatabase(androidApplication()) }
    single { get<ResourceDatabase>().libraryEntryDao() }
}

private fun createResourceDatabase(application: Application): ResourceDatabase {
    return Room.databaseBuilder(application, ResourceDatabase::class.java, "resources.db")
        .fallbackToDestructiveMigration()
        .build()
}
