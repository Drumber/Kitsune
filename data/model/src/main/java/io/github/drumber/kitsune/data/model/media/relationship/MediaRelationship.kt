package io.github.drumber.kitsune.data.model.media.relationship

import io.github.drumber.kitsune.data.model.media.Media

data class MediaRelationship(
    val id: String,
    val role: MediaRelationshipRole?,

    val media: Media?
)
