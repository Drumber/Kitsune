package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.model.Titles
import io.github.drumber.kitsune.data.presentation.dto.MediaUnitDto.UnitType
import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaUnitDto(
    val id: String,
    val type: UnitType,
    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val number: Int?,
    val length: String?,
    val thumbnail: ImageDto?,

    // Episode specific attributes
    val seasonNumber: Int?,
    val relativeNumber: Int?,
    val airdate: String?,

    // Chapter specific attributes
    val volumeNumber: Int?,
    val published: String?
) : Parcelable {
    enum class UnitType {
        Episode,
        Chapter
    }
}

fun MediaUnit.toMediaUnitDto() = when (this) {
    is Episode -> toMediaUnitDto()
    is Chapter -> toMediaUnitDto()
}

fun MediaUnitDto.toMediaUnit() = when (type) {
    UnitType.Episode -> toEpisode()
    UnitType.Chapter -> toChapter()
}

private fun Episode.toMediaUnitDto() = MediaUnitDto(
    id = id,
    type = UnitType.Episode,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    number = number,
    length = length,
    thumbnail = thumbnail?.toImageDto(),
    seasonNumber = seasonNumber,
    relativeNumber = relativeNumber,
    airdate = airdate,
    volumeNumber = null,
    published = null
)

private fun Chapter.toMediaUnitDto() = MediaUnitDto(
    id = id,
    type = UnitType.Chapter,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    number = number,
    length = length,
    thumbnail = thumbnail?.toImageDto(),
    seasonNumber = null,
    relativeNumber = null,
    airdate = null,
    volumeNumber = volumeNumber,
    published = published
)

private fun MediaUnitDto.toEpisode() = Episode(
    id = id,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    number = number,
    length = length,
    thumbnail = thumbnail?.toImage(),
    seasonNumber = seasonNumber,
    relativeNumber = relativeNumber,
    airdate = airdate
)

private fun MediaUnitDto.toChapter() = Chapter(
    id = id,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    number = number,
    length = length,
    thumbnail = thumbnail?.toImage(),
    volumeNumber = volumeNumber,
    published = published
)
