package io.github.drumber.kitsune.data.source.algolia

import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaImage(
    val tiny: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val original: String? = null,
    val meta: AlgoliaImageMeta? = null,
)

@Serializable
data class AlgoliaImageMeta(val dimensions: AlgoliaDimensions? = null)

@Serializable
data class AlgoliaDimensions(
    val tiny: AlgoliaDimension? = null,
    val small: AlgoliaDimension? = null,
    val medium: AlgoliaDimension? = null,
    val large: AlgoliaDimension? = null
)

@Serializable
data class AlgoliaDimension(val width: Int? = null, val height: Int? = null)
