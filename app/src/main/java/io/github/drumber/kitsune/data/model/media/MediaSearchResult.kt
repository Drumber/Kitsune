package io.github.drumber.kitsune.data.model.media

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaSearchResult(
    val id: Long,
    val kind: MediaSearchKind,
    val subtype: String,
    val slug: String,
    val titles: Titles? = null,
    val canonicalTitle: String? = null,
    val posterImage: Image? = null
)

@Serializable
enum class MediaSearchKind {
    @SerialName("anime")
    Anime,
    @SerialName("manga")
    Manga
}

fun MediaSearchResult.toMedia() = when (kind) {
    MediaSearchKind.Anime -> Anime(
        id = id.toString(),
        subtype = animeSubtypeFromString(subtype),
        slug = slug,
        titles = titles,
        canonicalTitle = canonicalTitle,
        posterImage = posterImage,
        abbreviatedTitles = null,
        ageRating = null,
        ageRatingGuide = null,
        averageRating = null,
        animeProduction = null,
        categories = null,
        coverImage = null,
        description = null,
        endDate = null,
        episodeCount = null,
        episodeLength = null,
        favoritesCount = null,
        mediaRelationships = null,
        nextRelease = null,
        nsfw = null,
        popularityRank = null,
        ratingFrequencies = null,
        ratingRank = null,
        startDate = null,
        status = null,
        streamingLinks = null,
        tba = null,
        totalLength = null,
        userCount = null,
        youtubeVideoId = null
    )
    MediaSearchKind.Manga -> Manga(
        id = id.toString(),
        subtype = mangaSubtypeFromString(subtype),
        slug = slug,
        titles = titles,
        canonicalTitle = canonicalTitle,
        posterImage = posterImage,
        userCount = null,
        totalLength = null,
        tba = null,
        status = null,
        startDate = null,
        ratingRank = null,
        ratingFrequencies = null,
        popularityRank = null,
        nsfw = null,
        nextRelease = null,
        mediaRelationships = null,
        favoritesCount = null,
        endDate = null,
        description = null,
        coverImage = null,
        categories = null,
        averageRating = null,
        ageRatingGuide = null,
        ageRating = null,
        abbreviatedTitles = null,
        chapterCount = null,
        serialization = null,
        volumeCount = null
    )
}

private fun animeSubtypeFromString(subtype: String) = AnimeSubtype.values()
    .find { it.name.equals(subtype, true) }

private fun mangaSubtypeFromString(subtype: String) = MangaSubtype.values()
    .find { it.name.equals(subtype, true) }
