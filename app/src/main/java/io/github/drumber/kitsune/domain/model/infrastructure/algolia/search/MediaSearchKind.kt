package io.github.drumber.kitsune.domain.model.infrastructure.algolia.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaSearchKind {
    @SerialName("anime")
    Anime,
    @SerialName("manga")
    Manga
}
