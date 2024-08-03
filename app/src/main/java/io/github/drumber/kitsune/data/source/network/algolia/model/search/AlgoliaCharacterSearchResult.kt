package io.github.drumber.kitsune.data.source.network.algolia.model.search

import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaCharacterSearchResult(
    val id: Long,
    val slug: String? = null,
    val canonicalName: String? = null,
    val image: AlgoliaImage? = null,
    val primaryMedia: String? = null
)
