package io.github.drumber.kitsune.data.source.local.character

import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.common.model.Image

data class LocalCharacter(
    val id: String,
    val slug: String?,
    val name: String?,
    val names: Titles?,
    val otherNames: List<String>?,
    val malId: Int?,
    val description: String?,
    val image: Image?
) {
    companion object {
        fun empty(id: String) = LocalCharacter(
            id = id,
            slug = null,
            name = null,
            names = null,
            otherNames = null,
            malId = null,
            description = null,
            image = null
        )
    }
}
