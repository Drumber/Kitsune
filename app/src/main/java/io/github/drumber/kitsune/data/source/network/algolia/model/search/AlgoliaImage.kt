package io.github.drumber.kitsune.data.source.network.algolia.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlgoliaImage(
    val tiny: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    @SerialName("tiny_webp")
    val tinyWebp: String? = null,
    @SerialName("small_webp")
    val smallWebp: String? = null,
    @SerialName("medium_webp")
    val mediumWebp: String? = null,
    @SerialName("large_webp")
    val largeWebp: String? = null,
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
    val large: AlgoliaDimension? = null,
    @SerialName("tiny_webp")
    val tinyWebp: AlgoliaDimension? = null,
    @SerialName("small_webp")
    val smallWebp: AlgoliaDimension? = null,
    @SerialName("medium_webp")
    val mediumWebp: AlgoliaDimension? = null,
    @SerialName("large_webp")
    val largeWebp: AlgoliaDimension? = null,
)

@Serializable
data class AlgoliaDimension(val width: Int? = null, val height: Int? = null)
