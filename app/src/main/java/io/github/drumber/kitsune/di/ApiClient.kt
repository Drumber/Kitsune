package io.github.drumber.kitsune.di

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.ResourceConverter
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.data.model.production.AnimeProduction
import io.github.drumber.kitsune.data.model.production.Casting
import io.github.drumber.kitsune.data.model.production.Character
import io.github.drumber.kitsune.data.model.production.Producer
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import io.github.drumber.kitsune.data.model.stats.Stats
import io.github.drumber.kitsune.data.model.streamer.Streamer
import io.github.drumber.kitsune.data.model.streamer.StreamingLink
import io.github.drumber.kitsune.data.model.unit.Chapter
import io.github.drumber.kitsune.data.model.unit.Episode
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.data.service.anime.EpisodesService
import io.github.drumber.kitsune.data.service.auth.AuthService
import io.github.drumber.kitsune.data.service.category.CategoryService
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.data.service.manga.ChaptersService
import io.github.drumber.kitsune.data.service.manga.MangaService
import io.github.drumber.kitsune.data.service.production.CastingService
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.util.network.AuthenticationInterceptor
import io.github.drumber.kitsune.util.network.AuthenticationInterceptorDummy
import io.github.drumber.kitsune.util.network.AuthenticationInterceptorImpl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

const val KITSU_API_URL = "https://kitsu.io/api/edge/"
const val KITSU_OAUTH_URL = "https://kitsu.io/api/oauth/"

val serviceModule = module {
    single { createHttpClient(get()) }
    single { createObjectMapper() }
    factory { createAuthService() }
    factory { createAuthenticationInterceptor() }
    factory {
        createService<AnimeService>(get(), get(),
            Anime::class.java,
            Manga::class.java,
            Category::class.java,
            AnimeProduction::class.java,
            Producer::class.java,
            StreamingLink::class.java,
            Streamer::class.java,
            MediaRelationship::class.java
        )
    }
    factory { createService<EpisodesService>(get(), get(), Episode::class.java) }
    factory {
        createService<MangaService>(get(), get(),
            Manga::class.java,
            Anime::class.java,
            Category::class.java,
            MediaRelationship::class.java
        )
    }
    factory { createService<ChaptersService>(get(), get(), Chapter::class.java) }
    factory { createService<CategoryService>(get(), get(), Category::class.java) }
    factory { createService<UserService>(get(), get(), User::class.java, Stats::class.java) }
    factory {
        createService<LibraryEntriesService>(get(), get(),
            LibraryEntry::class.java,
            Anime::class.java,
            Manga::class.java
        )
    }
    factory { createService<CastingService>(get(), get(), Casting::class.java, Character::class.java) }
}

private fun createHttpClientBuilder() = OkHttpClient.Builder()
    .apply { if(BuildConfig.DEBUG) addInterceptor(createHttpLoggingInterceptor()) }
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)

private fun createHttpClient(authenticationInterceptor: AuthenticationInterceptor) = createHttpClientBuilder()
    .addInterceptor(authenticationInterceptor)
    .build()

private fun createHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
    redactHeader("Authorization")
}

private fun Scope.createAuthenticationInterceptor() = try {
    AuthenticationInterceptorImpl(get())
} catch (t: Throwable) {
    // return dummy implementation if auth repository is not available (e.g. during unit test)
    AuthenticationInterceptorDummy()
}

private fun createAuthService() = createService<AuthService>(
    createHttpClientBuilder().build(),
    KITSU_OAUTH_URL
)

fun createObjectMapper(): ObjectMapper = jacksonObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)

private fun createConverterFactory(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    vararg classes: Class<*>
): JSONAPIConverterFactory {
    val resourceConverter = ResourceConverter(objectMapper, *classes)
    resourceConverter.setGlobalResolver { url ->
        val request = httpClient.newCall(Request.Builder().url(url).build())
        request.execute().body?.bytes()
    }
    return JSONAPIConverterFactory(resourceConverter)
}

private inline fun <reified T> createService(
    httpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    vararg classes: Class<*>,
    baseUrl: String = KITSU_API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(createConverterFactory(httpClient, objectMapper, *classes))
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(T::class.java)
}

private inline fun <reified T> createService(
    httpClient: OkHttpClient,
    baseUrl: String = KITSU_API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create(T::class.java)
}