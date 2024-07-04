package io.github.drumber.kitsune.data.source.network.mapping.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.mapping.model.NetworkMapping
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MappingApi {

    @GET("anime/{id}/mappings")
    suspend fun getAnimeMappings(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkMapping>>

    @GET("manga/{id}/mappings")
    suspend fun getMangaMappings(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkMapping>>

}