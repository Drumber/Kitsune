package io.github.drumber.kitsune.di

import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.auth.LogInUserUseCase
import io.github.drumber.kitsune.domain.auth.LogOutUserUseCase
import io.github.drumber.kitsune.domain.auth.RefreshAccessTokenIfExpiredUseCase
import io.github.drumber.kitsune.domain.auth.RefreshAccessTokenUseCase
import io.github.drumber.kitsune.domain.library.FetchLibraryEntriesForWidgetUseCase
import io.github.drumber.kitsune.domain.library.GetLibraryEntriesWithModificationsPagerUseCase
import io.github.drumber.kitsune.domain.library.SearchLibraryEntriesWithLocalModificationsPagerUseCase
import io.github.drumber.kitsune.domain.library.SynchronizeLocalLibraryModificationsUseCase
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryProgressUseCase
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryRatingUseCase
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryUseCase
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.domain.user.UpdateLocalUserUseCase
import io.github.drumber.kitsune.domain.work.UpdateLibraryWidgetUseCase
import org.koin.dsl.module

val domainModule = module {
    // Auth
    factory { IsUserLoggedInUseCase(get()) }
    factory { LogInUserUseCase(get(), get(), get()) }
    factory { LogOutUserUseCase(get(), get()) }
    factory { RefreshAccessTokenIfExpiredUseCase(get(), get()) }
    factory { RefreshAccessTokenUseCase(get(), get(), get()) }

    // User
    factory { GetLocalUserIdUseCase(get()) }
    factory { UpdateLocalUserUseCase(get(), get(), get()) }

    // Library
    factory { GetLibraryEntriesWithModificationsPagerUseCase(get(), get()) }
    factory { SearchLibraryEntriesWithLocalModificationsPagerUseCase(get(), get()) }
    factory { SynchronizeLocalLibraryModificationsUseCase(get(), get()) }
    factory { UpdateLibraryEntryProgressUseCase(get(), get()) }
    factory { UpdateLibraryEntryRatingUseCase(get()) }
    factory { UpdateLibraryEntryUseCase(get()) }
    factory { FetchLibraryEntriesForWidgetUseCase(get(), get()) }

    // Work
    factory { UpdateLibraryWidgetUseCase() }
}