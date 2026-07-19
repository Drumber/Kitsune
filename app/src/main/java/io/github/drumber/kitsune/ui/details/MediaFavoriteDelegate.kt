package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.exception.ResourceUpdateFailed
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Encapsulates the media favorite responsibility (loading and toggling the favorite) on behalf of
 * [DetailsViewModel].
 */
class MediaFavoriteDelegate(
    private val scope: CoroutineScope,
    private val favoriteRepository: FavoriteRepository,
    private val getLocalUserId: GetLocalUserIdUseCase,
    private val mediaProvider: () -> Media?
) {

    private val _favorite = MutableLiveData<Favorite?>()
    val favorite: LiveData<Favorite?>
        get() = _favorite

    suspend fun loadFavorite(media: Media) {
        val userId = getLocalUserId() ?: return

        val filter = Filter()
            .filter("user_id", userId)
            .filter("item_id", media.id)
            .filter("item_type", if (media is Anime) "Anime" else "Manga")

        try {
            val favorites = favoriteRepository.getAllFavorites(filter)
            _favorite.postValue(favorites?.firstOrNull())
        } catch (e: Exception) {
            logE("Failed to load favorites.", e)
        }
    }

    fun toggleFavorite() {
        val favorite = favorite.value

        scope.launch(Dispatchers.IO) {
            if (favorite == null) {
                val mediaItem = mediaProvider() ?: return@launch
                val userId = getLocalUserId() ?: return@launch

                try {
                    val newFavorite = favoriteRepository.createMediaFavorite(userId, mediaItem.mediaType, mediaItem.id)
                    _favorite.postValue(newFavorite)
                } catch (e: Exception) {
                    logE("Failed to create new favorite.", e)
                }
            } else {
                val favoriteId = favorite.id
                try {
                    val isSuccessful = favoriteRepository.deleteFavorite(favoriteId)
                    if (isSuccessful) {
                        _favorite.postValue(null)
                    } else {
                        throw ResourceUpdateFailed()
                    }
                } catch (e: Exception) {
                    logE("Failed to delete favorite.", e)
                }
            }
        }
    }
}
