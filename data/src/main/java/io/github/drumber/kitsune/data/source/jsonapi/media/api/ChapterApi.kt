package io.github.drumber.kitsune.data.source.jsonapi.media.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkChapter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ChapterApi {

    @GET("chapters")
    suspend fun getAllChapters(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkChapter>>

    @GET("chapters/{id}")
    suspend fun getChapter(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkChapter>

}