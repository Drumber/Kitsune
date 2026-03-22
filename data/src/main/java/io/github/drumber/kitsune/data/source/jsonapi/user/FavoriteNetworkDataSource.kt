package io.github.drumber.kitsune.data.source.jsonapi.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.user.api.FavoriteApi
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteNetworkDataSource(
    private val favoriteApi: FavoriteApi
) {

    suspend fun getAllFavorites(filter: Filter): List<NetworkFavorite>? {
        return withContext(Dispatchers.IO) {
            favoriteApi.getAllFavorites(filter.options).get()
        }
    }

    suspend fun createFavorite(favorite: NetworkFavorite): NetworkFavorite? {
        return withContext(Dispatchers.IO) {
            favoriteApi.postFavorite(JSONAPIDocument(favorite)).get()
        }
    }

    suspend fun deleteFavorite(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteApi.deleteFavorite(id).isSuccessful
        }
    }
}