package io.github.drumber.kitsune.data.presentation.model.media.production

import io.github.drumber.kitsune.data.source.network.character.NetworkCharacter

data class Casting(
    val id: String,
    val role: String?,
    val voiceActor: Boolean?,
    val featured: Boolean?,
    val language: String?,

    val character: NetworkCharacter?,
    val person: Person?
)
