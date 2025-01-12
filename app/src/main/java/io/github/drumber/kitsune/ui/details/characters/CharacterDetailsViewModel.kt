package io.github.drumber.kitsune.ui.details.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.shared.constants.Defaults
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.character.Character
import io.github.drumber.kitsune.data.model.user.Favorite
import io.github.drumber.kitsune.data.repository.media.CharacterRepository
import io.github.drumber.kitsune.data.repository.media.FavoriteRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.shared.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    private val characterRepository: CharacterRepository,
    private val getLocalUserId: GetLocalUserIdUseCase,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _characterFlow = MutableSharedFlow<Character>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val characterFlow
        get() = _characterFlow.asSharedFlow()

    private val _favoriteFlow = MutableSharedFlow<Favorite?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val favoriteFlow
        get() = _favoriteFlow.asSharedFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState
        get() = _uiState.asStateFlow()

    fun initCharacter(character: Character) {
        if (_characterFlow.replayCache.isNotEmpty() && _characterFlow.replayCache.firstOrNull()?.id == character.id) return

        viewModelScope.launch {
            _characterFlow.emit(character)

            launch(Dispatchers.IO) fetchFavorite@{
                val characterId = character.id
                val favorite = fetchFavorite(characterId)
                _favoriteFlow.emit(favorite)
            }

            launch(Dispatchers.IO) fetchFullCharacter@{
                // fetch full character model
                val characterId = character.id
                _uiState.emit(UiState(isLoadingMediaCharacters = true))
                val fullCharacter = try {
                    fetchCharacterData(characterId)
                } finally {
                    _uiState.emit(UiState(isLoadingMediaCharacters = false))
                }
                if (fullCharacter != null) {
                    _characterFlow.emit(fullCharacter)
                }
                _uiState.emit(UiState(hasMediaCharacters = fullCharacter?.mediaCharacters?.isNotEmpty() == true))
            }
        }
    }

    private suspend fun fetchCharacterData(id: String): Character? {
        val filter = Filter()
            .include("mediaCharacters", "mediaCharacters.media")
            .fields("media", *Defaults.MINIMUM_COLLECTION_FIELDS)

        return try {
            characterRepository.getCharacter(id, filter)
        } catch (e: Exception) {
            logE("Failed to fetch character data.", e)
            null
        }
    }

    private suspend fun fetchFavorite(characterId: String): Favorite? {
        val userId = getLocalUserId() ?: return null

        val filter = Filter()
            .filter("user_id", userId)
            .filter("item_id", characterId)
            .filter("item_type", "Character")

        return try {
            favoriteRepository.getAllFavorites(filter)?.firstOrNull()
        } catch (e: Exception) {
            logE("Failed to fetch favorites.", e)
            null
        }
    }

    fun toggleFavorite(): Boolean {
        val characterId = characterFlow.replayCache.firstOrNull()?.id ?: return false
        val userId = getLocalUserId() ?: return false
        val favorite = favoriteFlow.replayCache.firstOrNull()

        viewModelScope.launch(Dispatchers.IO) {
            if (favorite == null) {
                val addedFavorite = addToFavorites(userId, characterId)
                    ?: return@launch _favoriteFlow.emit(null)
                _favoriteFlow.emit(addedFavorite)
            } else {
                val favoriteId = favorite.id
                if (removeFromFavorites(favoriteId)) {
                    _favoriteFlow.emit(null)
                }
            }
        }
        return favorite == null
    }

    private suspend fun addToFavorites(userId: String, characterId: String): Favorite? {
        return try {
            favoriteRepository.createCharacterFavorite(userId, characterId)
        } catch (e: Exception) {
            logE("Failed to post favorite.", e)
            null
        }
    }

    private suspend fun removeFromFavorites(favoriteId: String): Boolean {
        return try {
            favoriteRepository.deleteFavorite(favoriteId)
        } catch (e: Exception) {
            logE("Failed to delete favorite.", e)
            false
        }
    }

}

data class UiState(
    val isLoadingMediaCharacters: Boolean = false,
    val hasMediaCharacters: Boolean = false
)
