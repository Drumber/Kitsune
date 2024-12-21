package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.graphql.fragment.LibraryMediaFragment

fun LibraryMediaFragment.toMedia() = when (type) {
    "Anime" -> toAnime()
    "Manga" -> toManga()
    else -> null
}

fun LibraryMediaFragment.toAnime() = Anime(
    id = id,
    slug = null,
    description = description.mapDescription(),
    titles = titles.toTitles(),
    canonicalTitle = titles.canonical,
    abbreviatedTitles = titles.alternatives,
    averageRating = averageRating?.toString(),
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = userCountRank,
    ratingRank = averageRatingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status.toReleaseStatus(),
    ageRating = ageRating?.toAgeRating(),
    ageRatingGuide = ageRatingGuide,
    nsfw = null,
    posterImage = posterImage?.fullImageFragment?.toImage(),
    coverImage = bannerImage?.fullImageFragment?.toImage(),
    totalLength = onAnime?.totalLength?.div(60),
    episodeCount = onAnime?.episodeCount,
    episodeLength = onAnime?.episodeLength?.div(60),
    youtubeVideoId = null,
    subtype = onAnime?.animeSubtype?.toAnimeSubtype(),
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

fun LibraryMediaFragment.toManga() = Manga(
    id = id,
    slug = null,
    description = description.mapDescription(),
    titles = titles.toTitles(),
    canonicalTitle = titles.canonical,
    abbreviatedTitles = titles.alternatives,
    averageRating = averageRating?.toString(),
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = userCountRank,
    ratingRank = averageRatingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status.toReleaseStatus(),
    ageRating = ageRating?.toAgeRating(),
    ageRatingGuide = ageRatingGuide,
    nsfw = null,
    posterImage = posterImage?.fullImageFragment?.toImage(),
    coverImage = bannerImage?.fullImageFragment?.toImage(),
    totalLength = null,
    chapterCount = onManga?.chapterCount,
    volumeCount = onManga?.volumeCount,
    subtype = onManga?.mangaSubtype?.toMangaSubtype(),
    serialization = null,
    categories = null,
    mediaRelationships = null
)

fun LibraryMediaFragment.Titles.toTitles() = titlesFragment.toTitles()
val LibraryMediaFragment.Titles.canonical get() = titlesFragment.canonical
val LibraryMediaFragment.Titles.alternatives get() = titlesFragment.alternatives

private fun Any.mapDescription() = (this as? Map<String, String>)?.let { descriptionMap ->
    descriptionMap["en"] ?: descriptionMap.values.firstOrNull()
}