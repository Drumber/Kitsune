package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.MediaMapper.toMedia
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacter
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacterRole
import io.github.drumber.kitsune.data.source.local.character.LocalCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkMediaCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkMediaCharacterRole

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

}