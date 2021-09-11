package io.github.drumber.kitsune.api.service

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.api.model.Episode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EpisodesService {

    @GET("episodes")
    fun allEpisodes(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): Call<JSONAPIDocument<List<Episode>>>

    @GET("episodes/{id}")
    fun getEpisode(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): Call<JSONAPIDocument<Episode>>

}