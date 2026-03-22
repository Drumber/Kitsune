package io.github.drumber.kitsune.data.source.algolia

import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaCharacterSearchResult(
    val id: Long,
    val slug: String? = null,
    val canonicalName: String? = null,
    val image: AlgoliaImage? = null,
    val primaryMedia: String? = null
)
