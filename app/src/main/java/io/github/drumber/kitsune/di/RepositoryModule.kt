package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.data.repository.AnimeRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AnimeRepository(get()) }
}