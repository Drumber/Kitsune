package io.github.drumber.kitsune.data.presentation.model.media.production

import io.github.drumber.kitsune.data.presentation.model.character.Character

data class Casting(
    val id: String,
    val role: String?,
    val voiceActor: Boolean?,
    val featured: Boolean?,
    val language: String?,

    val character: Character?,
    val person: Person?
)
