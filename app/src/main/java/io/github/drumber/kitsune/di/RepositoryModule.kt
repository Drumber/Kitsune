package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.domain.manager.AuthManager
import io.github.drumber.kitsune.domain.repository.AccessTokenRepository
import io.github.drumber.kitsune.domain.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.domain.repository.AnimeRepository
import io.github.drumber.kitsune.domain.repository.CastingRepository
import io.github.drumber.kitsune.domain.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.domain.repository.MangaRepository
import io.github.drumber.kitsune.domain.repository.MediaUnitRepository
import io.github.drumber.kitsune.domain.repository.UserRepository
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
    single { AuthManager(get(), get()) }
    single { AccessTokenRepository(get()) }
    single { AuthPreferences(androidContext(), get()) }
    single { UserRepository(get(), get(), get()) }
    single { UserPreferences(androidContext(), get()) }
    single { AlgoliaKeyRepository(get()) }
}
