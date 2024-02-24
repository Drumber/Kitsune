package io.github.drumber.kitsune.ui.details.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.model.infrastructure.character.Character
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.character.CharacterService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(private val service: CharacterService) : ViewModel() {

    private val _characterFlow =
        MutableSharedFlow<Character>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val characterFlow
        get() = _characterFlow.asSharedFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState
        get() = _uiState.asStateFlow()

    fun initCharacter(character: Character) {
        if (_characterFlow.replayCache.isNotEmpty() && _characterFlow.replayCache.firstOrNull()?.id == character.id) return

        viewModelScope.launch(Dispatchers.IO) {
            _characterFlow.emit(character)
            // fetch full character model
            val characterId = character.id ?: return@launch
            _uiState.emit(UiState(isLoading = true))
            val fullCharacter =  try {
                fetchCharacterData(characterId)
            } finally {
                _uiState.emit(UiState(isLoading = false))
            }
            if (fullCharacter != null) {
                _characterFlow.emit(fullCharacter)
            }
            _uiState.emit(UiState(hasMediaCharacters = fullCharacter?.mediaCharacters?.isNotEmpty() == true))
        }
    }

    private suspend fun fetchCharacterData(id: String): Character? {
        val filter = Filter()
            .include("mediaCharacters", "mediaCharacters.media")
            .fields("media", *Defaults.MINIMUM_COLLECTION_FIELDS)

        return try {
            service.getCharacter(id, filter.options).get()
        } catch (e: Exception) {
            logE("Failed to fetch character data.", e)
            null
        }
    }

}

data class UiState(
    val isLoading: Boolean = false,
    val hasMediaCharacters: Boolean = false
)
