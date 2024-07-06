package io.github.drumber.kitsune.data.source.network.algolia.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AlgoliaMediaSearchKind {
    @SerialName("anime")
    Anime,
    @SerialName("manga")
    Manga
}
