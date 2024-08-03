package io.github.drumber.kitsune.data.presentation.model.media.relationship

import io.github.drumber.kitsune.data.presentation.model.media.Media

data class MediaRelationship(
    val id: String,
    val role: MediaRelationshipRole?,

    val media: Media?
)
