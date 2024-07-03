package io.github.drumber.kitsune.data.source.network.media.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MangaApi {

    @GET("manga")
    suspend fun getAllManga(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkManga>>

    @GET("manga/{id}")
    suspend fun getManga(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkManga>

    @GET("trending/manga")
    suspend fun getTrending(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkManga>>

}