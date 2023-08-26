package io.github.drumber.kitsune.domain.service.production

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.production.Casting
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CastingService {

    @GET("castings")
    suspend fun allCastings(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Casting>>

    @GET("castings/{id}")
    suspend fun getCasting(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Casting>

}