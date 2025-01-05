package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.ImageDimension
import io.github.drumber.kitsune.data.common.model.ImageDimensions
import io.github.drumber.kitsune.data.common.model.ImageMeta
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageDto(
    val tiny: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val original: String?,
    val dimensions: ImageDimensionsDto?
) : Parcelable

@Parcelize
data class ImageDimensionsDto(
    val tiny: ImageDimensionDto?,
    val small: ImageDimensionDto?,
    val medium: ImageDimensionDto?,
    val large: ImageDimensionDto?
) : Parcelable

@Parcelize
data class ImageDimensionDto(val width: Int?, val height: Int?) : Parcelable

fun Image.toImageDto() = ImageDto(
    tiny = tiny,
    small = small,
    medium = medium,
    large = large,
    original = original,
    dimensions = meta?.dimensions?.toImageDimensionsDto()
)

fun ImageDimensions.toImageDimensionsDto() = ImageDimensionsDto(
    tiny = tiny?.toImageDimensionDto(),
    small = small?.toImageDimensionDto(),
    medium = medium?.toImageDimensionDto(),
    large = large?.toImageDimensionDto()
)

fun ImageDimension.toImageDimensionDto() = ImageDimensionDto(
    width = width,
    height = height
)

fun ImageDto.toImage() = Image(
    tiny = tiny,
    small = small,
    medium = medium,
    large = large,
    original = original,
    meta = ImageMeta(dimensions?.toImageDimensions())
)

fun ImageDimensionsDto.toImageDimensions() = ImageDimensions(
    tiny = tiny?.toImageDimension(),
    small = small?.toImageDimension(),
    medium = medium?.toImageDimension(),
    large = large?.toImageDimension()
)

fun ImageDimensionDto.toImageDimension() = ImageDimension(
    width = width,
    height = height
)
