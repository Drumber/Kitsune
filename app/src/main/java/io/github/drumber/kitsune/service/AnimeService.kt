package io.github.drumber.kitsune.service

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.model.Anime
import retrofit2.Call
import retrofit2.http.GET

interface AnimeService {

    @GET("anime")
    fun allAnime(): Call<JSONAPIDocument<List<Anime>>>

}