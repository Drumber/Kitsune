package io.github.drumber.kitsune.data.service.anime

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.unit.Episode
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EpisodesService {

    @GET("episodes")
    suspend fun allEpisodes(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Episode>>

    @GET("episodes/{id}")
    suspend fun getEpisode(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Episode>

}