package io.github.drumber.kitsune.data.source.network.algolia.model.search

import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaUserSearchResult(
    val id: Long,
    val name: String? = null,
    val slug: String? = null,
    val title: String? = null,
    val avatar: AlgoliaImage? = null,
    val followersCount: Int? = null
)
