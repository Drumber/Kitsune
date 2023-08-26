package io.github.drumber.kitsune.domain.model.infrastructure.algolia.search

import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimension
import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimensions
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.image.ImageMeta
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

fun AlgoliaImage.map() = Image(
    tiny, small, medium, large, original, meta?.map()
)

fun AlgoliaImageMeta.map() = ImageMeta(dimensions?.map())

fun AlgoliaDimensions.map() = Dimensions(
    tiny?.map(), small?.map(), medium?.map(), large?.map()
)

fun AlgoliaDimension.map() = Dimension(width, height)
