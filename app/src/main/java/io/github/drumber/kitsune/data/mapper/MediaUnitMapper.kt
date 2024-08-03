package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkMediaUnit

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
}