package io.github.drumber.kitsune.data.service.manga

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.unit.Chapter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ChaptersService {

    @GET("chapters")
    suspend fun allChapters(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Chapter>>

    @GET("chapters/{id}")
    suspend fun getChapter(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Chapter>

}