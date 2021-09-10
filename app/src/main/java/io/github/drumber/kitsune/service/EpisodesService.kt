package io.github.drumber.kitsune.service

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.model.Episode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EpisodesService {

    @GET("episodes")
    fun allEpisodes(): Call<JSONAPIDocument<List<Episode>>>

    @GET("episodes/{id}")
    fun getEpisode(@Path("id") id: String): Call<JSONAPIDocument<Episode>>

}