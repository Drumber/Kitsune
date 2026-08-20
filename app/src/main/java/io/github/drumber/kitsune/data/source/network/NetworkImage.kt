package io.github.drumber.kitsune.data.source.network

import com.fasterxml.jackson.annotation.JsonProperty

data class NetworkImage(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    @JsonProperty("tiny_webp")
    val tinyWebp: String? = null,
    @JsonProperty("small_webp")
    val smallWebp: String? = null,
    @JsonProperty("medium_webp")
    val mediumWebp: String? = null,
    @JsonProperty("large_webp")
    val largeWebp: String? = null,
    val original: String?,
    val meta: NetworkImageMeta?
)

data class NetworkImageMeta(val dimensions: NetworkImageDimensions?)

data class NetworkImageDimensions(
    val tiny: NetworkImageDimension?,
    val small: NetworkImageDimension?,
    val medium: NetworkImageDimension?,
    val large: NetworkImageDimension?,
    @JsonProperty("tiny_webp")
    val tinyWebp: NetworkImageDimension? = null,
    @JsonProperty("small_webp")
    val smallWebp: NetworkImageDimension? = null,
    @JsonProperty("medium_webp")
    val mediumWebp: NetworkImageDimension? = null,
    @JsonProperty("large_webp")
    val largeWebp: NetworkImageDimension? = null
)

data class NetworkImageDimension(val width: Int?, val height: Int?)
