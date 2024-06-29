package io.github.drumber.kitsune.data.source.network.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.Favorite
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

// TODO: change Favorite import
interface FavoriteApi {

    @GET("favorites")
    suspend fun getAllFavorites(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Favorite>>

    @POST("favorites")
    suspend fun postFavorite(
        @Body favorite: JSONAPIDocument<Favorite>
    ): JSONAPIDocument<Favorite>

    @DELETE("favorites/{id}")
    suspend fun deleteFavorite(
        @Path("id") id: String
    ): Response<Unit>

}