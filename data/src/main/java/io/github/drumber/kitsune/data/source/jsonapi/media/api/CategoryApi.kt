package io.github.drumber.kitsune.data.source.jsonapi.media.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.media.model.category.NetworkCategory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CategoryApi {

    @GET("categories")
    suspend fun getAllCategories(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkCategory>>

    @GET("categories/{id}")
    suspend fun getCategory(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkCategory>

}