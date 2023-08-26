package io.github.drumber.kitsune.domain.model.infrastructure.image

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val meta: ImageMeta?
) : Parcelable

@Parcelize
data class ImageMeta(val dimensions: Dimensions?) : Parcelable

@Parcelize
data class Dimensions(
    val tiny: Dimension?,
    val small: Dimension?,
    val medium: Dimension?,
    val large: Dimension?
) : Parcelable

@Parcelize
data class Dimension(val width: Int?, val height: Int?) : Parcelable
