package io.github.drumber.kitsune.api.service

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.api.model.Anime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface AnimeService {

    @GET("anime")
    fun allAnime(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): Call<JSONAPIDocument<List<Anime>>>

    @GET("anime/{id}")
    fun getAnime(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): Call<JSONAPIDocument<Anime>>

    @GET("trending/anime")
    fun trending(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): Call<JSONAPIDocument<List<Anime>>>

}