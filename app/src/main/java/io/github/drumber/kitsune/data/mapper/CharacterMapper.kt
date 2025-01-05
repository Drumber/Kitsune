package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.MediaMapper.toMedia
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.character.CharacterSearchResult
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacter
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacterRole
import io.github.drumber.kitsune.data.source.local.character.LocalCharacter
import io.github.drumber.kitsune.data.source.jsonapi.character.model.NetworkCharacter
import io.github.drumber.kitsune.data.source.jsonapi.character.model.NetworkMediaCharacter
import io.github.drumber.kitsune.data.source.jsonapi.character.model.NetworkMediaCharacterRole

object CharacterMapper {

    //********************************************************************************************//
    // From Network
    //********************************************************************************************//

    fun NetworkCharacter.toCharacter() = Character(
        id = id.require(),
        slug = slug,
        name = name,
        names = names,
        otherNames = otherNames,
        malId = malId,
        description = description,
        image = image,
        mediaCharacters = mediaCharacters?.map { it.toMediaCharacter() }
    )

    fun NetworkCharacter.toLocalCharacter() = LocalCharacter(
        id = id.require(),
        slug = slug,
        name = name,
        names = names,
        otherNames = otherNames,
        malId = malId,
        description = description,
        image = image
    )

    fun CharacterSearchResult.toCharacter() = Character(
        id = id,
        slug = slug,
        name = name,
        names = null,
        otherNames = null,
        malId = null,
        description = null,
        image = image,
        mediaCharacters = null
    )

    private fun NetworkMediaCharacter.toMediaCharacter() = MediaCharacter(
        id = id.require(),
        role = role?.toMediaCharacterRole(),
        media = media?.toMedia()
    )

    private fun NetworkMediaCharacterRole.toMediaCharacterRole() = when (this) {
        NetworkMediaCharacterRole.MAIN -> MediaCharacterRole.MAIN
        NetworkMediaCharacterRole.SUPPORTING -> MediaCharacterRole.SUPPORTING
        NetworkMediaCharacterRole.RECURRING -> MediaCharacterRole.RECURRING
        NetworkMediaCharacterRole.CAMEO -> MediaCharacterRole.CAMEO
    }

    //********************************************************************************************//
    // From Local
    //********************************************************************************************//

    fun LocalCharacter.toCharacter() = Character(
        id = id.require(),
        slug = slug,
        name = name,
        names = names,
        otherNames = otherNames,
        malId = malId,
        description = description,
        image = image,
        mediaCharacters = null
    )

    fun LocalCharacter.toNetworkCharacter() = NetworkCharacter(
        id = id,
        slug = slug,
        name = name,
        names = names,
        otherNames = otherNames,
        malId = malId,
        description = description,
        image = image,
        mediaCharacters = null
    )
}