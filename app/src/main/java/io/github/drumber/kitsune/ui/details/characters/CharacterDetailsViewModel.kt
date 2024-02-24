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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(private val service: CharacterService) : ViewModel() {

    private val _characterFlow =
        MutableSharedFlow<Character>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val characterFlow
        get() = _characterFlow.asSharedFlow()

    fun initCharacter(character: Character) {
        if (_characterFlow.replayCache.isNotEmpty() && _characterFlow.replayCache.firstOrNull()?.id == character.id) return

        viewModelScope.launch(Dispatchers.IO) {
            _characterFlow.emit(character)
            // fetch full character model
            character.id?.let { id -> fetchCharacterData(id) }?.let { character ->
                _characterFlow.emit(character)
            }
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