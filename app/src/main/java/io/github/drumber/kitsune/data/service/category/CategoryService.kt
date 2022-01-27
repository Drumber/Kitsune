package io.github.drumber.kitsune.data.service.category

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.category.Category
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CategoryService {

    @GET("categories")
    suspend fun allCategories(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Category>>

    @GET("categories/{id}")
    suspend fun getCategory(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Category>

}