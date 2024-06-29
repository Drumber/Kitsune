package io.github.drumber.kitsune.di

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.network.auth.AccessTokenNetworkDataSource
import io.github.drumber.kitsune.data.source.network.auth.api.AuthenticationApi
import io.github.drumber.kitsune.data.source.network.user.UserNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.api.UserApi
import io.github.drumber.kitsune.data.source.network.user.api.UserImageUploadApi
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUserImageUpload
import io.github.drumber.kitsune.domain_old.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.domain_old.repository.AnimeRepository
import io.github.drumber.kitsune.domain_old.repository.CastingRepository
import io.github.drumber.kitsune.domain_old.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.domain_old.repository.MangaRepository
import io.github.drumber.kitsune.domain_old.repository.MediaUnitRepository
import io.github.drumber.kitsune.preference.AuthPreference
import io.github.drumber.kitsune.preference.UserPreferences
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val repositoryModule = module {
    single { AnimeRepository(get()) }
    single { MangaRepository(get()) }
    single { MediaUnitRepository(get(), get()) }
    single { CastingRepository(get()) }
    single { LibraryEntriesRepository(get(), get()) }
//    single { AuthManager(get(), get()) }
//    single { AccessTokenRepository(get()) }
//    single { UserRepository(get(), get(), get()) }
    single { AlgoliaKeyRepository(get()) }

    // Auth
    factory { createAuthService(get()) }
    single { AccessTokenNetworkDataSource(get()) }
    single<AccessTokenLocalDataSource> { AuthPreference(androidContext(), get()) }
    single { AccessTokenRepository(get(), get()) }

    // User
    factory {
        createService<UserApi>(
            get(),
            get(),
            NetworkUser::class.java,
//            Stats::class.java,
//            Favorite::class.java,
//            Anime::class.java,
//            Manga::class.java,
//            Character::class.java
        )
    }
    factory {
        createService<UserImageUploadApi>(
            get(),
            get(),
            NetworkUserImageUpload::class.java
        )
    }
    single { UserNetworkDataSource(get()) }
    single<UserLocalDataSource> { UserPreferences(androidContext(), get()) }
    single { UserRepository(get(), get()) }
}

private fun createAuthService(objectMapper: ObjectMapper) = createService<AuthenticationApi>(
    OkHttpClient.Builder()
        .addNetworkInterceptor(createUserAgentInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build(),
    objectMapper,
    Kitsu.OAUTH_URL
)
