package io.github.drumber.kitsune.domain.model.database

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class DBImage(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    @Embedded(prefix = "meta_")
    val meta: DBImageMeta?
) : Parcelable

@Parcelize
data class DBImageMeta(@Embedded val dimensions: DBDimensions?) : Parcelable

@Parcelize
data class DBDimensions(
    @Embedded(prefix = "tiny_")
    val tiny: DBDimension?,
    @Embedded(prefix = "small_")
    val small: DBDimension?,
    @Embedded(prefix = "medium_")
    val medium: DBDimension?,
    @Embedded(prefix = "large_")
    val large: DBDimension?
) : Parcelable

@Parcelize
data class DBDimension(val width: Int?, val height: Int?) : Parcelable
