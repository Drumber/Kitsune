package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val meta: Meta?,
) : Parcelable

@Parcelize
data class Meta(val dimensions: Dimensions?) : Parcelable

@Parcelize
data class Dimensions(
    val tiny: Dimension?,
    val small: Dimension?,
    val medium: Dimension?,
    val large: Dimension?
) : Parcelable

@Parcelize
data class Dimension(val width: String?, val height: String?) : Parcelable
