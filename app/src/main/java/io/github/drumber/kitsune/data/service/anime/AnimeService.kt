package io.github.drumber.kitsune.data.service.anime

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.Anime
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

}