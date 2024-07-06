package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaDto(
    val id: String,
    val type: MediaType,
    val titles: Titles?,
    val canonicalTitle: String?,
    val posterImage: ImageDto?,
    val coverImage: ImageDto?
) : Parcelable

fun Media.toMediaDto() = MediaDto(
    id = id,
    type = when (this) {
        is Anime -> MediaType.Anime
        is Manga -> MediaType.Manga
    },
    titles = titles,
    canonicalTitle = canonicalTitle,
    posterImage = posterImage?.toImageDto(),
    coverImage = coverImage?.toImageDto()
)

fun MediaDto.toMedia(): Media {
    return when (type) {
        MediaType.Anime -> toAnime()
        MediaType.Manga -> toManga()
    }
}

private fun MediaDto.toAnime() = Anime(
    id = id,
    slug = null,
    description = null,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = null,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = null,
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = posterImage?.toImage(),
    coverImage = coverImage?.toImage(),
    totalLength = null,
    episodeCount = null,
    episodeLength = null,
    youtubeVideoId = null,
    subtype = null,
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

private fun MediaDto.toManga() = Manga(
    id = id,
    slug = null,
    description = null,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = null,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = null,
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = posterImage?.toImage(),
    coverImage = coverImage?.toImage(),
    totalLength = null,
    chapterCount = null,
    volumeCount = null,
    subtype = null,
    serialization = null,
    categories = null,
    mediaRelationships = null
)