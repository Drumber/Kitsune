package io.github.drumber.kitsune.data.source.jsonapi.media.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkEpisode
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface EpisodeApi {

    @GET("episodes")
    suspend fun getAllEpisodes(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkEpisode>>

    @GET("episodes/{id}")
    suspend fun getEpisode(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkEpisode>

}