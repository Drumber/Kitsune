package io.github.drumber.kitsune.di

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import io.github.drumber.kitsune.api.model.Anime
import io.github.drumber.kitsune.api.model.Episode
import io.github.drumber.kitsune.api.service.AnimeService
import io.github.drumber.kitsune.api.service.AuthService
import io.github.drumber.kitsune.api.service.EpisodesService
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

const val KITSU_API_URL = "https://kitsu.io/api/edge/"
const val KITSU_OAUTH_URL = "https://kitsu.io/api/oauth/"

val serviceModule = module {
    single { createObjectMapper() }
    factory { createService<AnimeService>(get(), Anime::class.java) }
    factory { createService<EpisodesService>(get(), Episode::class.java) }
    factory { createService<AuthService>(KITSU_OAUTH_URL) }
}

private inline fun createObjectMapper() = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

private inline fun createConverterFactory(
    objectMapper: ObjectMapper,
    vararg classes: Class<*>
): JSONAPIConverterFactory {
    return JSONAPIConverterFactory(objectMapper, *classes)
}

private inline fun <reified T> createService(
    objectMapper: ObjectMapper,
    vararg classes: Class<*>,
    baseUrl: String = KITSU_API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(createConverterFactory(objectMapper, *classes))
        .build()
        .create(T::class.java)
}

private inline fun <reified T> createService(
    baseUrl: String = KITSU_API_URL
): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create(T::class.java)
}