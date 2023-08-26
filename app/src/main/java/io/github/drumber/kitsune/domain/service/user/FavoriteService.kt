package io.github.drumber.kitsune.domain.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.user.Favorite
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface FavoriteService {

    @GET("favorites")
    suspend fun allFavorites(
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<List<Favorite>>

    @POST("favorites")
    suspend fun postFavorite(
        @Body favorite: JSONAPIDocument<Favorite>
    ): JSONAPIDocument<Favorite>

    @DELETE("favorites/{id}")
    fun deleteFavorite(
        @Path("id") id: String
    ): Call<ResponseBody>

}