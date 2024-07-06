package io.github.drumber.kitsune.data.source.network.algolia.model.search

import io.github.drumber.kitsune.data.common.Titles
import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaMediaSearchResult(
    val id: Long,
    val kind: AlgoliaMediaSearchKind,
    val subtype: String? = null,
    val slug: String? = null,
    val titles: Titles? = null,
    val canonicalTitle: String? = null,
    val posterImage: AlgoliaImage? = null
)
