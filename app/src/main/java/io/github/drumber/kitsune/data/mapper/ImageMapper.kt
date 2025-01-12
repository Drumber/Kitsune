package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.ImageDimension
import io.github.drumber.kitsune.data.model.ImageDimensions
import io.github.drumber.kitsune.data.model.ImageMeta
import io.github.drumber.kitsune.data.source.local.library.model.LocalDimension
import io.github.drumber.kitsune.data.source.local.library.model.LocalDimensions
import io.github.drumber.kitsune.data.source.local.library.model.LocalImage
import io.github.drumber.kitsune.data.source.local.library.model.LocalImageMeta

object ImageMapper {
    fun Image.toLocalImage() = LocalImage(
        tiny = tiny,
        small = small,
        medium = medium,
        large = large,
        original = original,
        meta = meta?.toLocalImageMeta()
    )

    fun ImageMeta.toLocalImageMeta() = LocalImageMeta(
        dimensions = dimensions?.toLocalDimensions()
    )

    fun ImageDimensions.toLocalDimensions() = LocalDimensions(
        tiny = tiny?.toLocalDimension(),
        small = small?.toLocalDimension(),
        medium = medium?.toLocalDimension(),
        large = large?.toLocalDimension()
    )

    fun ImageDimension.toLocalDimension() = LocalDimension(
        width = width,
        height = height
    )

    fun LocalImage.toImage() = Image(
        tiny = tiny,
        small = small,
        medium = medium,
        large = large,
        original = original,
        meta = meta?.toImageMeta()
    )

    fun LocalImageMeta.toImageMeta() = ImageMeta(
        dimensions = dimensions?.toImageDimensions()
    )

    fun LocalDimensions.toImageDimensions() = ImageDimensions(
        tiny = tiny?.toImageDimension(),
        small = small?.toImageDimension(),
        medium = medium?.toImageDimension(),
        large = large?.toImageDimension()
    )

    fun LocalDimension.toImageDimension() = ImageDimension(
        width = width,
        height = height
    )
}