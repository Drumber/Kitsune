package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.AuthRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.preference.AuthPreferences
import io.github.drumber.kitsune.preference.UserPreferences
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repositoryModule = module {
    single { AnimeRepository(get()) }
    single { MangaRepository(get()) }
    single { AuthRepository(get(), get()) }
    single { AuthPreferences(androidApplication(), get()) }
    single { UserRepository(get(), get(), get()) }
    single { UserPreferences(androidApplication(), get()) }
}