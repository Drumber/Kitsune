package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    @Embedded(prefix = "meta_") val meta: Meta?,
) : Parcelable

@Parcelize
data class Meta(@Embedded val dimensions: Dimensions?) : Parcelable

@Parcelize
data class Dimensions(
    @Embedded(prefix = "tiny_") val tiny: Dimension?,
    @Embedded(prefix = "small_") val small: Dimension?,
    @Embedded(prefix = "medium_") val medium: Dimension?,
    @Embedded(prefix = "large_") val large: Dimension?
) : Parcelable

@Parcelize
data class Dimension(val width: String?, val height: String?) : Parcelable
