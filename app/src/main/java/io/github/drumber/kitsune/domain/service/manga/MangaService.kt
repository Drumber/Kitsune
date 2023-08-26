package io.github.drumber.kitsune.domain.service.manga

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.media.Manga
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MangaService {

    @GET("manga")
    suspend fun allManga(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Manga>>

    @GET("manga/{id}")
    suspend fun getManga(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Manga>

    @GET("trending/manga")
    suspend fun trending(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Manga>>

}