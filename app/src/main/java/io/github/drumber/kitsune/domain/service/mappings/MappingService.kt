package io.github.drumber.kitsune.domain.service.mappings

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.mappings.Mapping
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface MappingService {

    @GET("anime/{id}/mappings")
    suspend fun getAnimeMappings(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Mapping>>

    @GET("manga/{id}/mappings")
    suspend fun getMangaMappings(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Mapping>>

}