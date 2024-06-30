package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.UserMapper.toFavorite
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.source.network.user.FavoriteNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavorite
import io.github.drumber.kitsune.domain_old.service.Filter

class FavoriteRepository(
    private val remoteFavoriteDataSource: FavoriteNetworkDataSource
) {

    suspend fun getAllFavorites(filter: Filter): List<Favorite>? {
        return remoteFavoriteDataSource.getAllFavorites(filter)?.map { it.toFavorite() }
    }

    suspend fun createFavorite(favorite: NetworkFavorite): Favorite? {
        return remoteFavoriteDataSource.createFavorite(favorite)?.toFavorite()
    }

    suspend fun deleteFavorite(id: String): Boolean {
        return remoteFavoriteDataSource.deleteFavorite(id)
    }
}