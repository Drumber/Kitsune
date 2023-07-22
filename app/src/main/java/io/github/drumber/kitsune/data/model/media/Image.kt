package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Image(
    val tiny: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val original: String? = null,
    @Embedded(prefix = "meta_") val meta: Meta? = null,
) : Parcelable

@Serializable
@Parcelize
data class Meta(@Embedded val dimensions: Dimensions? = null) : Parcelable

@Serializable
@Parcelize
data class Dimensions(
    @Embedded(prefix = "tiny_") val tiny: Dimension? = null,
    @Embedded(prefix = "small_") val small: Dimension? = null,
    @Embedded(prefix = "medium_") val medium: Dimension? = null,
    @Embedded(prefix = "large_") val large: Dimension? = null
) : Parcelable

@Serializable
@Parcelize
data class Dimension(val width: Int? = null, val height: Int? = null) : Parcelable
