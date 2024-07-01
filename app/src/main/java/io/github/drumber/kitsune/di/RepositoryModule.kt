package io.github.drumber.kitsune.di

import android.app.usage.NetworkStats
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.data.repository.ProfileLinkRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenLocalDataSource
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenPreference
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.UserPreferences
import io.github.drumber.kitsune.data.source.network.algolia.AlgoliaKeyNetworkDataSource
import io.github.drumber.kitsune.data.source.network.algolia.api.AlgoliaKeyApi
import io.github.drumber.kitsune.data.source.network.auth.AccessTokenNetworkDataSource
import io.github.drumber.kitsune.data.source.network.auth.api.AuthenticationApi
import io.github.drumber.kitsune.data.source.network.character.NetworkCharacter
import io.github.drumber.kitsune.data.source.network.user.FavoriteNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.ProfileLinkNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.UserNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.api.FavoriteApi
import io.github.drumber.kitsune.data.source.network.user.api.ProfileLinkApi
import io.github.drumber.kitsune.data.source.network.user.api.UserApi
import io.github.drumber.kitsune.data.source.network.user.api.UserImageUploadApi
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavorite
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUserImageUpload
import io.github.drumber.kitsune.data.source.network.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.source.network.user.model.profilelinks.NetworkProfileLinkSite
import io.github.drumber.kitsune.domain_old.repository.AnimeRepository
import io.github.drumber.kitsune.domain_old.repository.CastingRepository
import io.github.drumber.kitsune.domain_old.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.domain_old.repository.MangaRepository
import io.github.drumber.kitsune.domain_old.repository.MediaUnitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
//    single { AlgoliaKeyRepository(get()) }

    // Auth
    factory { createAuthService(get()) }
    single { AccessTokenNetworkDataSource(get()) }
    single<AccessTokenLocalDataSource> { AccessTokenPreference(androidContext(), get()) }
    single { AccessTokenRepository(get(), get()) }

    // User
    factory {
        createService<UserApi>(
            get(),
            get(),
            NetworkUser::class.java,
            NetworkStats::class.java,
            NetworkFavorite::class.java,
//            NetworkAnime::class.java, // TODO
//            NetworkManga::class.java,
            NetworkCharacter::class.java
        )
    }
    factory {
        createService<UserImageUploadApi>(
            get(),
            get(),
            NetworkUserImageUpload::class.java
        )
    }
    single { UserNetworkDataSource(get(), get()) }
    single<UserLocalDataSource> { UserPreferences(androidContext(), get()) }
    single { UserRepository(get(), get(), CoroutineScope(SupervisorJob() + Dispatchers.Default)) }

    // Algolia
    factory { createService<AlgoliaKeyApi>(get(), get()) }
    single { AlgoliaKeyNetworkDataSource(get()) }
    single { AlgoliaKeyRepository(get()) }

    // ProfileLinks
    factory {
        createService<ProfileLinkApi>(
            get(),
            get(),
            NetworkProfileLink::class.java,
            NetworkProfileLinkSite::class.java,
            NetworkUser::class.java
        )
    }
    single { ProfileLinkNetworkDataSource(get()) }
    single { ProfileLinkRepository(get()) }

    // Favorite
    factory {
        createService<FavoriteApi>(
            get(),
            get(),
            NetworkFavorite::class.java,
//            Anime::class.java, // TODO
//            Manga::class.java,
            NetworkUser::class.java
        )
    }
    single { FavoriteNetworkDataSource(get()) }
    single { FavoriteRepository(get()) }
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
