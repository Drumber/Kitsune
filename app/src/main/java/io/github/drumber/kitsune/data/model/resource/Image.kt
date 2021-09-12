package io.github.drumber.kitsune.data.model.resource

data class Image(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val meta: Meta?,
)

data class Meta(val dimensions: Dimensions?)

data class Dimensions(
    val tiny: Dimension?,
    val small: Dimension?,
    val medium: Dimension?,
    val large: Dimension?
)

data class Dimension(val width: String?, val height: String?)
