package io.github.drumber.kitsune.data.service.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface LibraryEntriesService {

    @GET("library-entries")
    suspend fun allLibraryEntries(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<LibraryEntry>>

    @GET("library-entries/{id}")
    suspend fun getLibraryEntry(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<LibraryEntry>

}