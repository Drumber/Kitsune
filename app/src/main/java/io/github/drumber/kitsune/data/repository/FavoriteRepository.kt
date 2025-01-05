package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.mapper.UserMapper.toFavorite
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.source.jsonapi.character.model.NetworkCharacter
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.jsonapi.user.FavoriteNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavorite
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavoriteItem
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser
import io.github.drumber.kitsune.data.common.Filter

class FavoriteRepository(
    private val remoteFavoriteDataSource: FavoriteNetworkDataSource
) {

    suspend fun getAllFavorites(filter: Filter): List<Favorite>? {
        return remoteFavoriteDataSource.getAllFavorites(filter)?.map { it.toFavorite() }
    }

    suspend fun createMediaFavorite(userId: String, mediaType: MediaType, mediaId: String): Favorite? {
        val favoriteItem = when (mediaType) {
            MediaType.Anime -> NetworkAnime.empty(mediaId)
            MediaType.Manga -> NetworkManga.empty(mediaId)
        }
        return createFavorite(userId, favoriteItem)
    }

    suspend fun createCharacterFavorite(userId: String, characterId: String): Favorite? {
        val favoriteItem = NetworkCharacter(id = characterId)
        return createFavorite(userId,favoriteItem)
    }

    private suspend fun createFavorite(userId: String, item: NetworkFavoriteItem): Favorite? {
        val newFavorite = NetworkFavorite(
            item = item,
            user = NetworkUser(id = userId)
        )
        return remoteFavoriteDataSource.createFavorite(newFavorite)?.toFavorite()
    }

    suspend fun deleteFavorite(id: String): Boolean {
        return remoteFavoriteDataSource.deleteFavorite(id)
    }
}