package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.model.media.unit.Chapter
import io.github.drumber.kitsune.data.model.media.unit.Episode
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkMediaUnit

object MediaUnitMapper {
    fun NetworkMediaUnit.toMediaUnit() = when (this) {
        is NetworkEpisode -> toEpisode()
        is NetworkChapter -> toChapter()
    }

    fun NetworkEpisode.toEpisode() = Episode(
        id = id.require(),
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        number = number,
        seasonNumber = seasonNumber,
        relativeNumber = relativeNumber,
        length = length,
        airdate = airdate,
        thumbnail = thumbnail
    )

    fun NetworkChapter.toChapter() = Chapter(
        id = id.require(),
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        number = number,
        volumeNumber = volumeNumber,
        length = length,
        thumbnail = thumbnail,
        published = published,
    )

    fun LocalNextMediaUnit.toMediaUnit(mediaType: LocalLibraryMedia.MediaType) = when (mediaType) {
        LocalLibraryMedia.MediaType.Anime -> toEpisode()
        LocalLibraryMedia.MediaType.Manga -> toChapter()
    }

    fun LocalNextMediaUnit.toEpisode() = Episode(
        id = unitId,
        description = null,
        titles = titles,
        canonicalTitle = canonicalTitle,
        number = number,
        seasonNumber = null,
        relativeNumber = null,
        length = null,
        airdate = null,
        thumbnail = thumbnail?.toImage()
    )

    fun LocalNextMediaUnit.toChapter() = Chapter(
        id = unitId,
        description = null,
        titles = titles,
        canonicalTitle = canonicalTitle,
        number = number,
        volumeNumber = null,
        length = null,
        thumbnail = thumbnail?.toImage(),
        published = null
    )
}