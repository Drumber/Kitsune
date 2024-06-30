package io.github.drumber.kitsune.data.source.network.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.user.api.FavoriteApi
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavorite
import io.github.drumber.kitsune.domain_old.service.Filter
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