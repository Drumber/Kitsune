package io.github.drumber.kitsune.domain.service.anime

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.media.Anime
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AnimeService {

    @GET("anime")
    suspend fun allAnime(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Anime>>

    @GET("anime/{id}")
    suspend fun getAnime(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Anime>

    @GET("trending/anime")
    suspend fun trending(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Anime>>

    // Will probably be replaced by origin_languages attribute in anime and manga
    // see: https://github.com/hummingbird-me/kitsu-server/commit/e730ef2e0482d37e7252496c9e937c3e1164bf08
    @GET("anime/{id}/_languages")
    suspend fun getLanguages(
        @Path("id") id: String
    ): List<String>

}