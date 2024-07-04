package io.github.drumber.kitsune.data.source.network.algolia.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaSearchKind {
    @SerialName("anime")
    Anime,
    @SerialName("manga")
    Manga
}
