package io.github.drumber.kitsune.data.presentation.dto

import android.os.Parcelable
import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.common.model.media.AgeRating
import io.github.drumber.kitsune.data.common.model.media.MediaType
import io.github.drumber.kitsune.data.common.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaDto(
    val id: String,
    val type: MediaType,
    val slug: String?,

    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,

    val averageRating: String?,
    val popularityRank: Int?,
    val ratingRank: Int?,

    val startDate: String?,
    val endDate: String?,
    val nextRelease: String?,
    val tba: String?,
    val status: ReleaseStatus?,

    val ageRating: AgeRating?,
    val ageRatingGuide: String?,

    val posterImage: ImageDto?,
    val coverImage: ImageDto?,

    val totalLength: Int?
) : Parcelable

fun Media.toMediaDto() = MediaDto(
    id = id,
    type = when (this) {
        is Anime -> MediaType.Anime
        is Manga -> MediaType.Manga
    },
    slug = slug,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = abbreviatedTitles,
    averageRating = averageRating,
    popularityRank = popularityRank,
    ratingRank = ratingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status,
    ageRating = ageRating,
    ageRatingGuide = ageRatingGuide,
    posterImage = posterImage?.toImageDto(),
    coverImage = coverImage?.toImageDto(),
    totalLength = totalLength
)

fun MediaDto.toMedia(): Media {
    return when (type) {
        MediaType.Anime -> toAnime()
        MediaType.Manga -> toManga()
    }
}

private fun MediaDto.toAnime() = Anime(
    id = id,
    slug = slug,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = abbreviatedTitles,
    averageRating = averageRating,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = popularityRank,
    ratingRank = ratingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status,
    ageRating = ageRating,
    ageRatingGuide = ageRatingGuide,
    nsfw = null,
    posterImage = posterImage?.toImage(),
    coverImage = coverImage?.toImage(),
    totalLength = totalLength,
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
    slug = slug,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = abbreviatedTitles,
    averageRating = averageRating,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = popularityRank,
    ratingRank = ratingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status,
    ageRating = ageRating,
    ageRatingGuide = ageRatingGuide,
    nsfw = null,
    posterImage = posterImage?.toImage(),
    coverImage = coverImage?.toImage(),
    totalLength = totalLength,
    chapterCount = null,
    volumeCount = null,
    subtype = null,
    serialization = null,
    categories = null,
    mediaRelationships = null
)