package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AnimeRepository(get()) }
    single { MangaRepository(get()) }
    single { AlgoliaKeyRepository(get()) }
}