package io.github.drumber.kitsune.di

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.constants.GitHub
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.AppUpdateRepository
import io.github.drumber.kitsune.data.repository.CastingRepository
import io.github.drumber.kitsune.data.repository.CategoryRepository
import io.github.drumber.kitsune.data.repository.CharacterRepository
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.data.repository.LibraryChangeListener
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.repository.MappingRepository
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.repository.ProfileLinkRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.repository.WidgetLibraryChangeListener
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenLocalDataSource
import io.github.drumber.kitsune.data.source.local.auth.AccessTokenPreference
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.UserPreferences
import io.github.drumber.kitsune.data.source.network.algolia.AlgoliaKeyNetworkDataSource
import io.github.drumber.kitsune.data.source.network.algolia.api.AlgoliaKeyApi
import io.github.drumber.kitsune.data.source.network.appupdate.AppReleaseNetworkDataSource
import io.github.drumber.kitsune.data.source.network.appupdate.api.GitHubApi
import io.github.drumber.kitsune.data.source.network.auth.AccessTokenNetworkDataSource
import io.github.drumber.kitsune.data.source.network.auth.api.AuthenticationApi
import io.github.drumber.kitsune.data.source.network.character.CharacterNetworkDataSource
import io.github.drumber.kitsune.data.source.network.character.api.CharacterApi
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkMediaCharacter
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.network.library.api.LibraryEntryApi
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.network.mapping.MappingNetworkDataSource
import io.github.drumber.kitsune.data.source.network.mapping.api.MappingApi
import io.github.drumber.kitsune.data.source.network.mapping.model.NetworkMapping
import io.github.drumber.kitsune.data.source.network.media.AnimeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.CastingNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.CategoryNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.ChapterNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.EpisodeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.MangaNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.api.AnimeApi
import io.github.drumber.kitsune.data.source.network.media.api.CastingApi
import io.github.drumber.kitsune.data.source.network.media.api.CategoryApi
import io.github.drumber.kitsune.data.source.network.media.api.ChapterApi
import io.github.drumber.kitsune.data.source.network.media.api.EpisodeApi
import io.github.drumber.kitsune.data.source.network.media.api.MangaApi
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.media.model.category.NetworkCategory
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkAnimeProduction
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkProducer
import io.github.drumber.kitsune.data.source.network.media.model.relationship.NetworkMediaRelationship
import io.github.drumber.kitsune.data.source.network.media.model.streamer.NetworkStreamer
import io.github.drumber.kitsune.data.source.network.media.model.streamer.NetworkStreamingLink
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode
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
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
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
            NetworkUserStats::class.java,
            NetworkFavorite::class.java,
            NetworkAnime::class.java,
            NetworkManga::class.java,
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
            NetworkAnime::class.java,
            NetworkManga::class.java,
            NetworkUser::class.java
        )
    }
    single { FavoriteNetworkDataSource(get()) }
    single { FavoriteRepository(get()) }

    // Anime
    factory {
        createService<AnimeApi>(
            get(), get(),
            NetworkAnime::class.java,
            NetworkManga::class.java,
            NetworkCategory::class.java,
            NetworkAnimeProduction::class.java,
            NetworkProducer::class.java,
            NetworkStreamingLink::class.java,
            NetworkStreamer::class.java,
            NetworkMediaRelationship::class.java
        )
    }
    single { AnimeNetworkDataSource(get()) }
    single { AnimeRepository(get()) }

    // Manga
    factory {
        createService<MangaApi>(
            get(), get(),
            NetworkManga::class.java,
            NetworkAnime::class.java,
            NetworkCategory::class.java,
            NetworkMediaRelationship::class.java
        )
    }
    single { MangaNetworkDataSource(get()) }
    single { MangaRepository(get()) }

    // Media Unit
    factory { createService<EpisodeApi>(get(), get(), NetworkEpisode::class.java) }
    factory { createService<ChapterApi>(get(), get(), NetworkChapter::class.java) }
    single { EpisodeNetworkDataSource(get()) }
    single { ChapterNetworkDataSource(get()) }
    single { MediaUnitRepository(get(), get()) }

    // Casting
    factory {
        createService<CastingApi>(
            get(),
            get(),
            NetworkCasting::class.java,
            NetworkCharacter::class.java
        )
    }
    single { CastingNetworkDataSource(get()) }
    single { CastingRepository(get()) }

    // Category
    factory { createService<CategoryApi>(get(), get(), NetworkCategory::class.java) }
    single { CategoryNetworkDataSource(get()) }
    single { CategoryRepository(get()) }

    // Character
    factory {
        createService<CharacterApi>(
            get(),
            get(),
            NetworkCharacter::class.java,
            NetworkMediaCharacter::class.java,
            NetworkAnime::class.java,
            NetworkManga::class.java
        )
    }
    single { CharacterNetworkDataSource(get()) }
    single { CharacterRepository(get()) }

    // Mapping
    factory {
        createService<MappingApi>(
            get(),
            get(),
            NetworkMapping::class.java
        )
    }
    single { MappingNetworkDataSource(get()) }
    single { MappingRepository(get()) }

    // Library Entry
    factory {
        createService<LibraryEntryApi>(
            get(), get(),
            NetworkLibraryEntry::class.java,
            NetworkAnime::class.java,
            NetworkManga::class.java
        )
    }
    single { LibraryNetworkDataSource(get()) }
    single { LibraryLocalDataSource(get()) }
    single<LibraryChangeListener> { WidgetLibraryChangeListener(androidApplication(), get()) }
    single {
        LibraryRepository(
            get(),
            get(),
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    // App Update
    factory {
        createService<GitHubApi>(
            get(named("unauthenticated")),
            get(),
            GitHub.API_URL
        )
    }
    single { AppReleaseNetworkDataSource(get()) }
    single { AppUpdateRepository(get()) }
}

private fun createAuthService(objectMapper: ObjectMapper) = createService<AuthenticationApi>(
    createHttpClientBuilder(addLoggingInterceptor = false).build(),
    objectMapper,
    Kitsu.OAUTH_URL
)
