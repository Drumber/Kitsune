package io.github.drumber.kitsune.domain.mapper

import io.github.drumber.kitsune.domain.model.database.LocalManga
import io.github.drumber.kitsune.domain.model.infrastructure.media.Manga

fun Manga.toLocalManga() = LocalManga(
    id = id,
    slug = slug,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = abbreviatedTitles,
    averageRating = averageRating,
    ratingFrequencies = ratingFrequencies,
    userCount = userCount,
    favoritesCount = favoritesCount,
    popularityRank = popularityRank,
    ratingRank = ratingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status,
    ageRating = ageRating,
    ageRatingGuide = ageRatingGuide,
    nsfw = nsfw,
    posterImage = posterImage?.toDBImage(),
    coverImage = coverImage?.toDBImage(),
    totalLength = totalLength,
    chapterCount = chapterCount,
    volumeCount = volumeCount,
    subtype = subtype,
    serialization = serialization
)

fun LocalManga.toManga() = Manga(
    id = id,
    slug = slug,
    description = description,
    titles = titles,
    canonicalTitle = canonicalTitle,
    abbreviatedTitles = abbreviatedTitles,
    averageRating = averageRating,
    ratingFrequencies = ratingFrequencies,
    userCount = userCount,
    favoritesCount = favoritesCount,
    popularityRank = popularityRank,
    ratingRank = ratingRank,
    startDate = startDate,
    endDate = endDate,
    nextRelease = nextRelease,
    tba = tba,
    status = status,
    ageRating = ageRating,
    ageRatingGuide = ageRatingGuide,
    nsfw = nsfw,
    posterImage = posterImage?.toImage(),
    coverImage = coverImage?.toImage(),
    totalLength = totalLength,
    chapterCount = chapterCount,
    volumeCount = volumeCount,
    subtype = subtype,
    serialization = serialization,
    categories = null,
    mediaRelationships = null
)
