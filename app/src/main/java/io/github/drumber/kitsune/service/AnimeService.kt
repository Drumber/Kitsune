package io.github.drumber.kitsune.service

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.model.Anime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AnimeService {

    @GET("anime")
    fun allAnime(): Call<JSONAPIDocument<List<Anime>>>

    @GET("anime/{id}")
    fun getAnime(@Path("id") id: String): Call<JSONAPIDocument<Anime>>

}