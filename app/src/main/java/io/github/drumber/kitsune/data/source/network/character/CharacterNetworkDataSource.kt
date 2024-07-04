package io.github.drumber.kitsune.data.source.network.character

import io.github.drumber.kitsune.data.source.network.character.api.CharacterApi
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter
import io.github.drumber.kitsune.domain_old.service.Filter
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