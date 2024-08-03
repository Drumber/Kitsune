package io.github.drumber.kitsune.data.source.network.library.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface LibraryEntryApi {

    @GET("library-entries")
    suspend fun getAllLibraryEntries(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkLibraryEntry>>

    @GET("library-entries/{id}")
    suspend fun getLibraryEntry(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkLibraryEntry>

    @PATCH("library-entries/{id}")
    suspend fun updateLibraryEntry(
        @Path("id") id: String,
        @Body libraryEntry: JSONAPIDocument<NetworkLibraryEntry>,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkLibraryEntry>

    @POST("library-entries")
    suspend fun postLibraryEntry(
        @Body libraryEntry: JSONAPIDocument<NetworkLibraryEntry>,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkLibraryEntry>

    @DELETE("library-entries/{id}")
    suspend fun deleteLibraryEntry(
        @Path("id") id: String
    ): Response<Unit>

}