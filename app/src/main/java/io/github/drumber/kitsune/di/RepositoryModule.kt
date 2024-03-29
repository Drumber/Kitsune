package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.domain.repository.*
import io.github.drumber.kitsune.preference.AuthPreferences
import io.github.drumber.kitsune.preference.UserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { AnimeRepository(get()) }
    single { MangaRepository(get()) }
    single { MediaUnitRepository(get(), get()) }
    single { CastingRepository(get()) }
    single { LibraryEntriesRepository(get(), get()) }
    single { AuthRepository(get(), get()) }
    single { AuthPreferences(androidContext(), get()) }
    single { UserRepository(get(), get(), get()) }
    single { UserPreferences(androidContext(), get()) }
    single { AlgoliaKeyRepository(get()) }
}
