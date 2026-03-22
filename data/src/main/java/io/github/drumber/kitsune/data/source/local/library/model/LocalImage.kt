package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded

data class LocalImage(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    @Embedded(prefix = "meta_")
    val meta: LocalImageMeta?
)

data class LocalImageMeta(@Embedded val dimensions: LocalDimensions?)

data class LocalDimensions(
    @Embedded(prefix = "tiny_")
    val tiny: LocalDimension?,
    @Embedded(prefix = "small_")
    val small: LocalDimension?,
    @Embedded(prefix = "medium_")
    val medium: LocalDimension?,
    @Embedded(prefix = "large_")
    val large: LocalDimension?
)

data class LocalDimension(val width: Int?, val height: Int?)
