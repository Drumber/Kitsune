package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.source.graphql.type.MediaTypeEnum

fun MediaType.toMediaTypeEnum() = when (this) {
    MediaType.Anime -> MediaTypeEnum.ANIME
    MediaType.Manga -> MediaTypeEnum.MANGA
}