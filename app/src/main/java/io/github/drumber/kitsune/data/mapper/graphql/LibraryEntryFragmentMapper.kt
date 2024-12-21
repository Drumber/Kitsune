package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.graphql.fragment.LibraryEntryFragment

fun LibraryEntryFragment.toLibraryEntry() = LibraryEntry(
    id = id,
    updatedAt = updatedAt,
    startedAt = startedAt,
    finishedAt = finishedAt,
    progressedAt = progressedAt,
    status = status.toLibraryStatus(),
    progress = progress,
    reconsuming = reconsuming,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = rating,
    notes = notes,
    privateEntry = private,
    reactionSkipped = null,
    media = media.toMedia()
)

fun LibraryEntryFragment.Media.toMedia() = when (type) {
    "Anime" -> toAnime()
    "Manga" -> toManga()
    else -> null
}

fun LibraryEntryFragment.Media.toAnime() = Anime(
    id = id,
    slug = slug,
    description = null,
    titles = titles.toTitles(),
    canonicalTitle = titles.canonical,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = startDate,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = status.toReleaseStatus(),
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = posterImage?.fullImageFragment?.toImage(),
    coverImage = bannerImage?.fullImageFragment?.toImage(),
    totalLength = null,
    episodeCount = onAnime?.episodeCount,
    episodeLength = null,
    youtubeVideoId = null,
    subtype = onAnime?.animeSubtype?.toAnimeSubtype(),
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

fun LibraryEntryFragment.Media.toManga() = Manga(
    id = id,
    slug = slug,
    description = null,
    titles = titles.toTitles(),
    canonicalTitle = titles.canonical,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = startDate,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = status.toReleaseStatus(),
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = posterImage?.fullImageFragment?.toImage(),
    coverImage = bannerImage?.fullImageFragment?.toImage(),
    totalLength = null,
    chapterCount = onManga?.chapterCount,
    volumeCount = null,
    subtype = onManga?.mangaSubtype?.toMangaSubtype(),
    serialization = null,
    categories = null,
    mediaRelationships = null
)

fun LibraryEntryFragment.Titles.toTitles() = titlesFragment.toTitles()

val LibraryEntryFragment.Titles.canonical get() = titlesFragment.canonical