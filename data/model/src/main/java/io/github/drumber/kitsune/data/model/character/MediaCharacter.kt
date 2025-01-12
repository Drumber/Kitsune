package io.github.drumber.kitsune.data.model.character

import io.github.drumber.kitsune.data.model.media.Media

data class MediaCharacter(
    val id: String,
    val role: MediaCharacterRole?,
    val media: Media?
)
