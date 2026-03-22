package io.github.drumber.kitsune.data.source.jsonapi.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavorite
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface FavoriteApi {

    @GET("favorites")
    suspend fun getAllFavorites(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<NetworkFavorite>>

    @POST("favorites")
    suspend fun postFavorite(
        @Body favorite: JSONAPIDocument<NetworkFavorite>
    ): JSONAPIDocument<NetworkFavorite>

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(
        @Path("id") id: String
    ): Response<Unit>

}