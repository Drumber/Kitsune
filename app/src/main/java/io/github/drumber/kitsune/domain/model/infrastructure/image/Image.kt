package io.github.drumber.kitsune.domain.model.infrastructure.image

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val tiny: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val original: String? = null,
    val meta: ImageMeta? = null,
) : Parcelable

@Parcelize
data class ImageMeta(val dimensions: Dimensions? = null) : Parcelable

@Parcelize
data class Dimensions(
    val tiny: Dimension? = null,
    val small: Dimension? = null,
    val medium: Dimension? = null,
    val large: Dimension? = null
) : Parcelable

@Parcelize
data class Dimension(val width: Int? = null, val height: Int? = null) : Parcelable
