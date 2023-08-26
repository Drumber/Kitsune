package io.github.drumber.kitsune.domain.service.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.library.LibraryEntry
import retrofit2.Response
import retrofit2.http.*

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

    @PATCH("library-entries/{id}")
    suspend fun updateLibraryEntry(
        @Path("id") id: String,
        @Body libraryEntry: JSONAPIDocument<LibraryEntry>
    ): JSONAPIDocument<LibraryEntry>

    @POST("library-entries")
    suspend fun postLibraryEntry(
        @Body libraryEntry: JSONAPIDocument<LibraryEntry>
    ): JSONAPIDocument<LibraryEntry>

    @DELETE("library-entries/{id}")
    suspend fun deleteLibraryEntry(
        @Path("id") id: String
    ): Response<Unit>

}