package io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search

import kotlinx.serialization.Serializable

@Serializable
data class CharacterSearchResult(
    val id: Long,
    val slug: String? = null,
    val canonicalName: String? = null,
    val image: AlgoliaImage? = null,
    val primaryMedia: String? = null
)
