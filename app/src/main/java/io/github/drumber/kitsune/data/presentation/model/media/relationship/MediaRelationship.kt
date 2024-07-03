package io.github.drumber.kitsune.data.presentation.model.media.relationship

import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia

data class MediaRelationship(
    val id: String,
    val role: MediaRelationshipRole?,

    val media: NetworkMedia?
)
