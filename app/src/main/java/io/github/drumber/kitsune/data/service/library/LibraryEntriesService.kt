package io.github.drumber.kitsune.data.service.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import okhttp3.ResponseBody
import retrofit2.Call
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
    fun deleteLibraryEntry(
        @Path("id") id: String
    ): Call<ResponseBody>

}