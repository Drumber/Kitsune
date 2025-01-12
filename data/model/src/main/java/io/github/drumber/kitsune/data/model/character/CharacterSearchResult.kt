package io.github.drumber.kitsune.data.model.character

import io.github.drumber.kitsune.data.model.Image

data class CharacterSearchResult(
    val id: String,
    val slug: String?,
    val name: String?,
    val image: Image?,
    val primaryMediaTitle: String?
)