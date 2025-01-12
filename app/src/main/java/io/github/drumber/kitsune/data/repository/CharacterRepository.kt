package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.model.character.Character
import io.github.drumber.kitsune.data.source.jsonapi.character.CharacterNetworkDataSource
import io.github.drumber.kitsune.data.model.Filter

class CharacterRepository(
    private val characterNetworkDataSource: CharacterNetworkDataSource
) {

    suspend fun getCharacter(id: String, filter: Filter): Character? {
        return characterNetworkDataSource.getCharacter(id, filter)?.toCharacter()
    }
}