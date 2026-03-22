package io.github.drumber.kitsune.data.source.jsonapi.media.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.media.model.production.NetworkCasting
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CastingApi {

    @GET("castings")
    suspend fun getAllCastings(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkCasting>>

    @GET("castings/{id}")
    suspend fun getCasting(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkCasting>

}