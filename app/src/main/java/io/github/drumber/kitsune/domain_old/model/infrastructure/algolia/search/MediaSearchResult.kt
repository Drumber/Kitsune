package io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search

import io.github.drumber.kitsune.domain_old.model.common.media.Titles
import kotlinx.serialization.Serializable

@Serializable
data class MediaSearchResult(
    val id: Long,
    val kind: MediaSearchKind,
    val subtype: String? = null,
    val slug: String? = null,
    val titles: Titles? = null,
    val canonicalTitle: String? = null,
    val posterImage: AlgoliaImage? = null
)
