package io.github.drumber.kitsune.data.presentation.model.character

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.Titles
import io.github.drumber.kitsune.data.presentation.model.user.FavoriteItem

data class Character(
    val id: String,
    val slug: String?,
    val name: String?,
    val names: Titles?,
    val otherNames: List<String>?,
    val malId: Int?,
    val description: String?,
    val image: Image?,

    val mediaCharacters: List<MediaCharacter>?
) : FavoriteItem
