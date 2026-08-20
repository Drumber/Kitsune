package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.config.ImageConfig
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.ImageDimension
import io.github.drumber.kitsune.data.common.ImageDimensions
import io.github.drumber.kitsune.data.common.ImageMeta
import io.github.drumber.kitsune.data.source.local.library.model.LocalDimension
import io.github.drumber.kitsune.data.source.local.library.model.LocalDimensions
import io.github.drumber.kitsune.data.source.local.library.model.LocalImage
import io.github.drumber.kitsune.data.source.local.library.model.LocalImageMeta
import io.github.drumber.kitsune.data.source.network.NetworkImage
import io.github.drumber.kitsune.data.source.network.NetworkImageDimension
import io.github.drumber.kitsune.data.source.network.NetworkImageDimensions
import io.github.drumber.kitsune.data.source.network.NetworkImageMeta

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

    fun Image.toNetworkImage() = NetworkImage(
        tiny = tiny,
        small = small,
        medium = medium,
        large = large,
        original = original,
        meta = meta?.toNetworkImageMeta()
    )

    fun ImageMeta.toNetworkImageMeta() = NetworkImageMeta(
        dimensions = dimensions?.toNetworkDimensions()
    )

    fun ImageDimensions.toNetworkDimensions() = NetworkImageDimensions(
        tiny = tiny?.toNetworkDimension(),
        small = small?.toNetworkDimension(),
        medium = medium?.toNetworkDimension(),
        large = large?.toNetworkDimension()
    )

    fun ImageDimension.toNetworkDimension() = NetworkImageDimension(
        width = width,
        height = height
    )

    fun NetworkImage.toImage() = when (ImageConfig.useWebp) {
        true -> toImageWebp()
        false -> toImageRegular()
    }

    private fun NetworkImage.toImageRegular() = Image(
        tiny = tiny,
        small = small,
        medium = medium,
        large = large,
        original = original,
        meta = meta?.toImageMetaRegular()
    )

    private fun NetworkImage.toImageWebp() = Image(
        tiny = tinyWebp ?: tiny,
        small = smallWebp ?: small,
        medium = mediumWebp ?: medium,
        large = largeWebp ?: large,
        original = original,
        meta = meta?.toImageMetaWebp()
    )

    private fun NetworkImageMeta.toImageMetaRegular() = ImageMeta(dimensions?.toImageDimensionsRegular())

    private fun NetworkImageMeta.toImageMetaWebp() = ImageMeta(dimensions?.toImageDimensionsWebp())

    private fun NetworkImageDimensions.toImageDimensionsRegular() = ImageDimensions(
        tiny = tiny?.toImageDimension(),
        small = small?.toImageDimension(),
        medium = medium?.toImageDimension(),
        large = large?.toImageDimension(),
    )

    private fun NetworkImageDimensions.toImageDimensionsWebp() = ImageDimensions(
        tiny = tinyWebp?.toImageDimension() ?: tiny?.toImageDimension(),
        small = smallWebp?.toImageDimension() ?: small?.toImageDimension(),
        medium = mediumWebp?.toImageDimension() ?: medium?.toImageDimension(),
        large = largeWebp?.toImageDimension() ?: large?.toImageDimension(),
    )

    private fun NetworkImageDimension.toImageDimension() = ImageDimension(width, height)
}
