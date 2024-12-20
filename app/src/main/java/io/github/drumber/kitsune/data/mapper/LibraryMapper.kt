package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.mapper.ImageMapper.toLocalImage
import io.github.drumber.kitsune.data.mapper.MediaMapper.toAnime
import io.github.drumber.kitsune.data.mapper.MediaMapper.toAnimeSubtype
import io.github.drumber.kitsune.data.mapper.MediaMapper.toManga
import io.github.drumber.kitsune.data.mapper.MediaMapper.toMangaSubtype
import io.github.drumber.kitsune.data.mapper.MediaMapper.toRatingFrequencies
import io.github.drumber.kitsune.data.mapper.MediaMapper.toReleaseStatus
import io.github.drumber.kitsune.data.mapper.MediaUnitMapper.toMediaUnit
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.presentation.model.library.LibraryModificationState
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.ReactionSkip
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalReactionSkip
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryStatus
import io.github.drumber.kitsune.data.source.network.library.model.NetworkReactionSkip
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga

object LibraryMapper {
    fun LocalLibraryEntry.toLibraryEntry() = LibraryEntry(
        id = id,
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        progressedAt = progressedAt,
        status = status?.toLibraryStatus(),
        progress = progress,
        reconsuming = reconsuming,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
        reactionSkipped = reactionSkipped?.toReactionSkip(),
        media = media?.toMedia()
    )

    fun NetworkLibraryEntry.toLibraryEntry() = LibraryEntry(
        id = id.require(),
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        progressedAt = progressedAt,
        status = status?.toLibraryStatus(),
        progress = progress,
        reconsuming = reconsuming,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
        reactionSkipped = reactionSkipped?.toReactionSkip(),
        media = when {
            anime != null -> anime.toAnime()
            manga != null -> manga.toManga()
            else -> null
        }
    )

    fun NetworkLibraryEntry.toLocalLibraryEntry() = LocalLibraryEntry(
        id = id.require(),
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        progressedAt = progressedAt,
        status = status?.toLocalLibraryStatus(),
        progress = progress,
        reconsuming = reconsuming,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
        reactionSkipped = reactionSkipped?.toLocalReactionSkip(),
        media = when {
            anime != null -> anime.toLocalLibraryMedia()
            manga != null -> manga.toLocalLibraryMedia()
            else -> null
        }
    )

    fun LibraryEntry.toLocalLibraryEntry() = LocalLibraryEntry(
        id = id,
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        progressedAt = progressedAt,
        status = status?.toLocalLibraryStatus(),
        progress = progress,
        reconsuming = reconsuming,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
        reactionSkipped = reactionSkipped?.toLocalReactionSkip(),
        media = media?.toLocalLibraryMedia()
    )

    fun LocalLibraryEntryModification.toLibraryEntryModification() = LibraryEntryModification(
        id = id,
        createTime = createTime,
        state = state.toLibraryModificationState(),
        startedAt = startedAt,
        finishedAt = finishedAt,
        status = status?.toLibraryStatus(),
        progress = progress,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
    )

    fun LibraryEntryModification.toLocalLibraryEntryModification() = LocalLibraryEntryModification(
        id = id,
        createTime = createTime,
        state = state.toLocalLibraryModificationState(),
        startedAt = startedAt,
        finishedAt = finishedAt,
        status = status?.toLocalLibraryStatus(),
        progress = progress,
        reconsumeCount = reconsumeCount,
        volumesOwned = volumesOwned,
        ratingTwenty = ratingTwenty,
        notes = notes,
        privateEntry = privateEntry,
    )

    fun LocalLibraryEntryWithModificationAndNextMediaUnit.toLibraryEntryWithModificationAndNextUnit() =
        LibraryEntryWithModificationAndNextUnit(
            libraryEntryWithModification = LibraryEntryWithModification(
                libraryEntry = libraryEntry.toLibraryEntry(),
                modification = libraryEntryModification?.toLibraryEntryModification()
            ),
            nextUnit = libraryEntry.media?.type?.let { nextMediaUnit?.toMediaUnit(it) }
        )

    fun LocalLibraryMedia.toMedia() = when (type) {
        LocalLibraryMedia.MediaType.Anime -> toAnime()
        LocalLibraryMedia.MediaType.Manga -> toManga()
    }

    fun LocalLibraryMedia.toAnime() = Anime(
        id = id,
        slug = null,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies,
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
        nsfw = nsfw,
        posterImage = posterImage?.toImage(),
        coverImage = coverImage?.toImage(),
        totalLength = totalLength,
        episodeCount = episodeCount,
        episodeLength = episodeLength,
        youtubeVideoId = null,
        subtype = animeSubtype,
        categories = null,
        animeProduction = null,
        streamingLinks = null,
        mediaRelationships = null
    )

    fun LocalLibraryMedia.toManga() = Manga(
        id = id,
        slug = null,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies,
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
        nsfw = nsfw,
        posterImage = posterImage?.toImage(),
        coverImage = coverImage?.toImage(),
        totalLength = totalLength,
        chapterCount = chapterCount,
        volumeCount = volumeCount,
        subtype = mangaSubtype,
        serialization = serialization,
        categories = null,
        mediaRelationships = null
    )

    fun NetworkAnime.toLocalLibraryMedia() = LocalLibraryMedia(
        id = id,
        type = LocalLibraryMedia.MediaType.Anime,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies?.toRatingFrequencies(),
        popularityRank = popularityRank,
        ratingRank = ratingRank,
        startDate = startDate,
        endDate = endDate,
        nextRelease = nextRelease,
        tba = tba,
        status = status?.toReleaseStatus(),
        ageRating = ageRating,
        ageRatingGuide = ageRatingGuide,
        nsfw = nsfw,
        posterImage = posterImage?.toLocalImage(),
        coverImage = coverImage?.toLocalImage(),
        animeSubtype = subtype?.toAnimeSubtype(),
        totalLength = totalLength,
        episodeCount = episodeCount,
        episodeLength = episodeLength,
        mangaSubtype = null,
        chapterCount = null,
        volumeCount = null,
        serialization = null
    )

    fun NetworkManga.toLocalLibraryMedia() = LocalLibraryMedia(
        id = id,
        type = LocalLibraryMedia.MediaType.Manga,
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies?.toRatingFrequencies(),
        popularityRank = popularityRank,
        ratingRank = ratingRank,
        startDate = startDate,
        endDate = endDate,
        nextRelease = nextRelease,
        tba = tba,
        status = status?.toReleaseStatus(),
        ageRating = ageRating,
        ageRatingGuide = ageRatingGuide,
        nsfw = nsfw,
        posterImage = posterImage?.toLocalImage(),
        coverImage = coverImage?.toLocalImage(),
        animeSubtype = null,
        totalLength = null,
        episodeCount = null,
        episodeLength = null,
        mangaSubtype = subtype?.toMangaSubtype(),
        chapterCount = chapterCount,
        volumeCount = volumeCount,
        serialization = serialization
    )

    fun Media.toLocalLibraryMedia() = when (this) {
        is Anime -> toLocalLibraryMedia()
        is Manga -> toLocalLibraryMedia()
    }

    fun Anime.toLocalLibraryMedia() = LocalLibraryMedia(
        id = id,
        type = mediaType.toLocalMediaType(),
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies,
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
        posterImage = posterImage?.toLocalImage(),
        coverImage = coverImage?.toLocalImage(),
        animeSubtype = subtype,
        totalLength = totalLength,
        episodeCount = episodeCount,
        episodeLength = episodeLength,
        mangaSubtype = null,
        chapterCount = null,
        volumeCount = null,
        serialization = null
    )

    fun Manga.toLocalLibraryMedia() = LocalLibraryMedia(
        id = id,
        type = mediaType.toLocalMediaType(),
        description = description,
        titles = titles,
        canonicalTitle = canonicalTitle,
        abbreviatedTitles = abbreviatedTitles,
        averageRating = averageRating,
        ratingFrequencies = ratingFrequencies,
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
        posterImage = posterImage?.toLocalImage(),
        coverImage = coverImage?.toLocalImage(),
        animeSubtype = null,
        totalLength = null,
        episodeCount = null,
        episodeLength = null,
        mangaSubtype = subtype,
        chapterCount = chapterCount,
        volumeCount = volumeCount,
        serialization = serialization
    )

    fun MediaType.toLocalMediaType(): LocalLibraryMedia.MediaType = when (this) {
        MediaType.Anime -> LocalLibraryMedia.MediaType.Anime
        MediaType.Manga -> LocalLibraryMedia.MediaType.Manga
    }

    fun NetworkReactionSkip.toLocalReactionSkip(): LocalReactionSkip = when (this) {
        NetworkReactionSkip.Unskipped -> LocalReactionSkip.Unskipped
        NetworkReactionSkip.Skipped -> LocalReactionSkip.Skipped
        NetworkReactionSkip.Ignored -> LocalReactionSkip.Ignored
    }

    fun NetworkLibraryStatus.toLocalLibraryStatus(): LocalLibraryStatus = when (this) {
        NetworkLibraryStatus.Current -> LocalLibraryStatus.Current
        NetworkLibraryStatus.Planned -> LocalLibraryStatus.Planned
        NetworkLibraryStatus.Completed -> LocalLibraryStatus.Completed
        NetworkLibraryStatus.OnHold -> LocalLibraryStatus.OnHold
        NetworkLibraryStatus.Dropped -> LocalLibraryStatus.Dropped
    }

    fun NetworkLibraryStatus.toLibraryStatus(): LibraryStatus = when (this) {
        NetworkLibraryStatus.Current -> LibraryStatus.Current
        NetworkLibraryStatus.Planned -> LibraryStatus.Planned
        NetworkLibraryStatus.Completed -> LibraryStatus.Completed
        NetworkLibraryStatus.OnHold -> LibraryStatus.OnHold
        NetworkLibraryStatus.Dropped -> LibraryStatus.Dropped
    }

    fun LibraryStatus.toLocalLibraryStatus(): LocalLibraryStatus = when (this) {
        LibraryStatus.Current -> LocalLibraryStatus.Current
        LibraryStatus.Planned -> LocalLibraryStatus.Planned
        LibraryStatus.Completed -> LocalLibraryStatus.Completed
        LibraryStatus.OnHold -> LocalLibraryStatus.OnHold
        LibraryStatus.Dropped -> LocalLibraryStatus.Dropped
    }

    fun LibraryStatus.toNetworkLibraryStatus(): NetworkLibraryStatus = when (this) {
        LibraryStatus.Current -> NetworkLibraryStatus.Current
        LibraryStatus.Planned -> NetworkLibraryStatus.Planned
        LibraryStatus.Completed -> NetworkLibraryStatus.Completed
        LibraryStatus.OnHold -> NetworkLibraryStatus.OnHold
        LibraryStatus.Dropped -> NetworkLibraryStatus.Dropped
    }

    fun LocalLibraryStatus.toLibraryStatus(): LibraryStatus = when (this) {
        LocalLibraryStatus.Current -> LibraryStatus.Current
        LocalLibraryStatus.Planned -> LibraryStatus.Planned
        LocalLibraryStatus.Completed -> LibraryStatus.Completed
        LocalLibraryStatus.OnHold -> LibraryStatus.OnHold
        LocalLibraryStatus.Dropped -> LibraryStatus.Dropped
    }

    fun NetworkReactionSkip.toReactionSkip(): ReactionSkip = when (this) {
        NetworkReactionSkip.Unskipped -> ReactionSkip.Unskipped
        NetworkReactionSkip.Skipped -> ReactionSkip.Skipped
        NetworkReactionSkip.Ignored -> ReactionSkip.Ignored
    }

    fun LocalReactionSkip.toReactionSkip(): ReactionSkip = when (this) {
        LocalReactionSkip.Unskipped -> ReactionSkip.Unskipped
        LocalReactionSkip.Skipped -> ReactionSkip.Skipped
        LocalReactionSkip.Ignored -> ReactionSkip.Ignored
    }

    fun ReactionSkip.toLocalReactionSkip(): LocalReactionSkip = when (this) {
        ReactionSkip.Unskipped -> LocalReactionSkip.Unskipped
        ReactionSkip.Skipped -> LocalReactionSkip.Skipped
        ReactionSkip.Ignored -> LocalReactionSkip.Ignored
    }

    fun LocalLibraryModificationState.toLibraryModificationState(): LibraryModificationState =
        when (this) {
            LocalLibraryModificationState.SYNCHRONIZING -> LibraryModificationState.SYNCHRONIZING
            LocalLibraryModificationState.NOT_SYNCHRONIZED -> LibraryModificationState.NOT_SYNCHRONIZED
        }

    fun LibraryModificationState.toLocalLibraryModificationState(): LocalLibraryModificationState =
        when (this) {
            LibraryModificationState.SYNCHRONIZING -> LocalLibraryModificationState.SYNCHRONIZING
            LibraryModificationState.NOT_SYNCHRONIZED -> LocalLibraryModificationState.NOT_SYNCHRONIZED
        }
}