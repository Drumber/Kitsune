package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.presentation.model.character.Character
import kotlinx.parcelize.Parcelize

@Parcelize
data class CharacterDto(
    val id: String,
    val slug: String?,
    val name: String?,
    val names: Titles?,
    val otherNames: List<String>?,
    val malId: Int?,
    val description: String?,
    val image: ImageDto?
) : Parcelable

fun Character.toCharacterDto() = CharacterDto(
    id = id,
    slug = slug,
    name = name,
    names = names,
    otherNames = otherNames,
    malId = malId,
    description = description,
    image = image?.toImageDto()
)

fun CharacterDto.toCharacter() = Character(
    id = id,
    slug = slug,
    name = name,
    names = names,
    otherNames = otherNames,
    malId = malId,
    description = description,
    image = image?.toImage(),
    mediaCharacters = null
)
