package io.github.drumber.kitsune.data.source.algolia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AlgoliaMediaSearchKind {
    @SerialName("anime")
    Anime,
    @SerialName("manga")
    Manga
}
