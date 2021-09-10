package io.github.drumber.kitsune.di

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import io.github.drumber.kitsune.model.Anime
import io.github.drumber.kitsune.model.Episode
import io.github.drumber.kitsune.service.AnimeService
import io.github.drumber.kitsune.service.EpisodesService
import org.koin.dsl.module
import retrofit2.Retrofit

const val KITSU_API_URL = "https://kitsu.io/api/edge/"

val serviceModule = module {
    single { createObjectMapper() }
    factory { createService<AnimeService>(get(), Anime::class.java) }
    factory { createService<EpisodesService>(get(), Episode::class.java) }
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
    vararg classes: Class<*>
): T {
    return Retrofit.Builder()
        .baseUrl(KITSU_API_URL)
        .addConverterFactory(createConverterFactory(objectMapper, *classes))
        .build()
        .create(T::class.java)
}