package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.ImageDimension
import io.github.drumber.kitsune.data.common.ImageDimensions
import io.github.drumber.kitsune.data.common.ImageMeta
import io.github.drumber.kitsune.data.common.media.AnimeSubtype
import io.github.drumber.kitsune.data.common.media.MangaSubtype
import io.github.drumber.kitsune.data.presentation.model.algolia.AlgoliaKey
import io.github.drumber.kitsune.data.presentation.model.algolia.AlgoliaKeyCollection
import io.github.drumber.kitsune.data.presentation.model.character.CharacterSearchResult
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.algolia.AlgoliaCharacterSearchResult
import io.github.drumber.kitsune.data.source.algolia.AlgoliaDimension
import io.github.drumber.kitsune.data.source.algolia.AlgoliaDimensions
import io.github.drumber.kitsune.data.source.algolia.AlgoliaImage
import io.github.drumber.kitsune.data.source.algolia.AlgoliaImageMeta
import io.github.drumber.kitsune.data.source.algolia.AlgoliaMediaSearchKind
import io.github.drumber.kitsune.data.source.algolia.AlgoliaMediaSearchResult
import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKey
import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKeyCollection

object AlgoliaMapper {
    fun NetworkAlgoliaKeyCollection.toAlgoliaKeyCollection() = AlgoliaKeyCollection(
        users = users?.toAlgoliaKey(),
        posts = posts?.toAlgoliaKey(),
        media = media?.toAlgoliaKey(),
        groups = groups?.toAlgoliaKey(),
        characters = characters?.toAlgoliaKey()
    )

    fun NetworkAlgoliaKey.toAlgoliaKey() = AlgoliaKey(
        key = key.require(),
        index = index
    )

    fun AlgoliaMediaSearchResult.toMedia() = when (kind) {
        AlgoliaMediaSearchKind.Anime -> toAnime()
        AlgoliaMediaSearchKind.Manga -> toManga()
    }

    private fun AlgoliaMediaSearchResult.toAnime() = Anime(
        id = id.toString(),
        subtype = animeSubtypeFromString(subtype),
        slug = slug,
        titles = titles,
        canonicalTitle = canonicalTitle,
        posterImage = posterImage?.toImage(),
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

    private fun AlgoliaMediaSearchResult.toManga() = Manga(
        id = id.toString(),
        subtype = mangaSubtypeFromString(subtype),
        slug = slug,
        titles = titles,
        canonicalTitle = canonicalTitle,
        posterImage = posterImage?.toImage(),
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

    fun AlgoliaCharacterSearchResult.toCharacterSearchResult() = CharacterSearchResult(
        id = id.toString(),
        slug = slug,
        name = canonicalName,
        image = image?.toImage(),
        primaryMediaTitle = primaryMedia
    )

    fun AlgoliaImage.toImage() = Image(
        tiny, small, medium, large, original, meta?.toImageMeta()
    )

    fun AlgoliaImageMeta.toImageMeta() = ImageMeta(dimensions?.toDimensions())

    fun AlgoliaDimensions.toDimensions() = ImageDimensions(
        tiny?.toDimension(), small?.toDimension(), medium?.toDimension(), large?.toDimension()
    )

    fun AlgoliaDimension.toDimension() = ImageDimension(width, height)

    private fun animeSubtypeFromString(subtype: String?) = AnimeSubtype.entries
        .find { it.name.equals(subtype, true) }

    private fun mangaSubtypeFromString(subtype: String?) = MangaSubtype.entries
        .find { it.name.equals(subtype, true) }
}