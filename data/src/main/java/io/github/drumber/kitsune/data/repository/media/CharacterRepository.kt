package io.github.drumber.kitsune.data.repository.media

import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.character.Character
import io.github.drumber.kitsune.data.source.jsonapi.character.CharacterNetworkDataSource

class CharacterRepository(
    private val characterNetworkDataSource: CharacterNetworkDataSource
) {

    suspend fun getCharacter(id: String, filter: Filter): Character? {
        return characterNetworkDataSource.getCharacter(id, filter)?.toCharacter()
    }
}