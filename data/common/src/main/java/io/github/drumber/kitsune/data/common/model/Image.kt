package io.github.drumber.kitsune.data.common.model

data class Image(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val meta: ImageMeta?
) {
    fun smallOrHigher(): String? {
        return small ?: medium ?: large ?: original
    }

    fun largeOrDown(): String? {
        return large ?: medium ?: small ?: tiny
    }

    fun originalOrDown(): String? {
        return original ?: largeOrDown()
    }
}

data class ImageMeta(val dimensions: ImageDimensions?)

data class ImageDimensions(
    val tiny: ImageDimension?,
    val small: ImageDimension?,
    val medium: ImageDimension?,
    val large: ImageDimension?
)

data class ImageDimension(val width: Int?, val height: Int?)
