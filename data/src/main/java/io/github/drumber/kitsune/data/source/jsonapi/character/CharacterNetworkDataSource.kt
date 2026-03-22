package io.github.drumber.kitsune.data.source.jsonapi.character

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.character.api.CharacterApi
import io.github.drumber.kitsune.data.source.jsonapi.character.model.NetworkCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CharacterNetworkDataSource(
    private val characterApi: CharacterApi
) {

    suspend fun getCharacter(id: String, filter: Filter): NetworkCharacter? {
        return withContext(Dispatchers.IO) {
            characterApi.getCharacter(id, filter.options).get()
        }
    }
}