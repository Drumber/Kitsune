package io.github.drumber.kitsune.data.source.local.character

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles

data class LocalCharacter(
    val id: String,
    val slug: String?,
    val name: String?,
    val names: Titles?,
    val otherNames: List<String>?,
    val malId: Int?,
    val description: String?,
    val image: Image?
)
