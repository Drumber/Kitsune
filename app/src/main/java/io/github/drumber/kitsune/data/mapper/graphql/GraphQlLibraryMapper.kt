package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.media.AnimeSubtype
import io.github.drumber.kitsune.data.common.media.MangaSubtype
import io.github.drumber.kitsune.data.common.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesWithNextUnitQuery
import io.github.drumber.kitsune.data.source.graphql.type.AnimeSubtypeEnum
import io.github.drumber.kitsune.data.source.graphql.type.LibraryEntrySortEnum
import io.github.drumber.kitsune.data.source.graphql.type.LibraryEntryStatusEnum
import io.github.drumber.kitsune.data.source.graphql.type.MangaSubtypeEnum
import io.github.drumber.kitsune.data.source.graphql.type.ReleaseStatusEnum
import io.github.drumber.kitsune.data.source.graphql.type.SortDirection

fun GetLibraryEntriesWithNextUnitQuery.All.toLibraryEntriesWithModificationAndNextUnit() = nodes
    ?.filterNotNull()
    ?.map(GetLibraryEntriesWithNextUnitQuery.Node::toLibraryEntriesWithModificationAndNextUnit)

fun GetLibraryEntriesWithNextUnitQuery.Node.toLibraryEntriesWithModificationAndNextUnit() =
    LibraryEntryWithModificationAndNextUnit(
        LibraryEntryWithModification(
            toLibraryEntry(),
            null
        ),
        nextUnit?.toMediaUnit(media.type)
    )

fun GetLibraryEntriesWithNextUnitQuery.Node.toLibraryEntry() = LibraryEntry(
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

fun LibraryEntryStatusEnum.toLibraryStatus() = when (this) {
    LibraryEntryStatusEnum.CURRENT -> LibraryStatus.Current
    LibraryEntryStatusEnum.PLANNED -> LibraryStatus.Planned
    LibraryEntryStatusEnum.COMPLETED -> LibraryStatus.Completed
    LibraryEntryStatusEnum.ON_HOLD -> LibraryStatus.OnHold
    LibraryEntryStatusEnum.DROPPED -> LibraryStatus.Dropped
    LibraryEntryStatusEnum.UNKNOWN__ -> null
}

fun LibraryStatus.toLibraryEntryStatusEnum() = when (this) {
    LibraryStatus.Current -> LibraryEntryStatusEnum.CURRENT
    LibraryStatus.Planned -> LibraryEntryStatusEnum.PLANNED
    LibraryStatus.Completed -> LibraryEntryStatusEnum.COMPLETED
    LibraryStatus.OnHold -> LibraryEntryStatusEnum.ON_HOLD
    LibraryStatus.Dropped -> LibraryEntryStatusEnum.DROPPED
}

fun GetLibraryEntriesWithNextUnitQuery.Media.toMedia() = when (type) {
    "Anime" -> toAnime()
    "Manga" -> toManga()
    else -> null
}

fun GetLibraryEntriesWithNextUnitQuery.Media.toAnime() = Anime(
    id = id,
    slug = slug,
    description = null,
    titles = titles.localized as? Map<String, String>,
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
    posterImage = posterImage?.views?.toImage(posterImage.original.url),
    coverImage = Image(
        tiny = null,
        small = null,
        medium = null,
        large = null,
        original = bannerImage?.original?.url,
        meta = null
    ),
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

fun GetLibraryEntriesWithNextUnitQuery.Media.toManga() = Manga(
    id = id,
    slug = slug,
    description = null,
    titles = titles.localized as? Map<String, String>,
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
    posterImage = posterImage?.views?.toImage(posterImage.original.url),
    coverImage = Image(
        tiny = null,
        small = null,
        medium = null,
        large = null,
        original = bannerImage?.original?.url,
        meta = null
    ),
    totalLength = null,
    chapterCount = onManga?.chapterCount,
    volumeCount = null,
    subtype = onManga?.mangaSubtype?.toMangaSubtype(),
    serialization = null,
    categories = null,
    mediaRelationships = null
)

fun GetLibraryEntriesWithNextUnitQuery.NextUnit.toMediaUnit(mediaType: String) =
    when (mediaType) {
        "Anime" -> toEpisode()
        "Manga" -> toChapter()
        else -> null
    }

fun GetLibraryEntriesWithNextUnitQuery.NextUnit.toEpisode() = Episode(
    id = id,
    description = null,
    titles = titles.localized as? Map<String, String>,
    canonicalTitle = titles.canonical,
    number = number,
    seasonNumber = null,
    relativeNumber = null,
    length = null,
    airdate = null,
    thumbnail = Image(
        tiny = null,
        small = null,
        medium = null,
        large = null,
        original = thumbnail?.original?.url,
        meta = null
    )
)

fun GetLibraryEntriesWithNextUnitQuery.NextUnit.toChapter() = Chapter(
    id = id,
    description = null,
    titles = titles.localized as? Map<String, String>,
    canonicalTitle = titles.canonical,
    number = number,
    volumeNumber = null,
    length = null,
    thumbnail = Image(
        tiny = null,
        small = null,
        medium = null,
        large = null,
        original = thumbnail?.original?.url,
        meta = null
    ),
    published = null
)

fun List<GetLibraryEntriesWithNextUnitQuery.View>.toImage(original: String? = null) = Image(
    tiny = firstOrNull { it.name == "tiny" }?.url,
    small = firstOrNull { it.name == "small" }?.url,
    medium = firstOrNull { it.name == "medium" }?.url,
    large = firstOrNull { it.name == "large" }?.url,
    original = original,
    meta = null
)

fun AnimeSubtypeEnum.toAnimeSubtype() = when (this) {
    AnimeSubtypeEnum.TV -> AnimeSubtype.TV
    AnimeSubtypeEnum.SPECIAL -> AnimeSubtype.Special
    AnimeSubtypeEnum.OVA -> AnimeSubtype.OVA
    AnimeSubtypeEnum.ONA -> AnimeSubtype.ONA
    AnimeSubtypeEnum.MOVIE -> AnimeSubtype.Movie
    AnimeSubtypeEnum.MUSIC -> AnimeSubtype.Music
    AnimeSubtypeEnum.UNKNOWN__ -> null
}

fun MangaSubtypeEnum.toMangaSubtype() = when (this) {
    MangaSubtypeEnum.MANGA -> MangaSubtype.Manga
    MangaSubtypeEnum.NOVEL -> MangaSubtype.Novel
    MangaSubtypeEnum.MANHUA -> MangaSubtype.Manhua
    MangaSubtypeEnum.ONESHOT -> MangaSubtype.Oneshot
    MangaSubtypeEnum.DOUJIN -> MangaSubtype.Doujin
    MangaSubtypeEnum.MANHWA -> MangaSubtype.Manhwa
    MangaSubtypeEnum.OEL -> MangaSubtype.Oel
    MangaSubtypeEnum.UNKNOWN__ -> null
}

fun ReleaseStatusEnum.toReleaseStatus() = when (this) {
    ReleaseStatusEnum.TBA -> ReleaseStatus.TBA
    ReleaseStatusEnum.FINISHED -> ReleaseStatus.Finished
    ReleaseStatusEnum.CURRENT -> ReleaseStatus.Current
    ReleaseStatusEnum.UPCOMING -> ReleaseStatus.Upcoming
    ReleaseStatusEnum.UNRELEASED -> ReleaseStatus.Unreleased
    ReleaseStatusEnum.UNKNOWN__ -> null
}

fun LibraryFilterOptions.SortBy.toLibraryEntrySortEnum() = when (this) {
    LibraryFilterOptions.SortBy.STATUS -> LibraryEntrySortEnum.STATUS
    LibraryFilterOptions.SortBy.STARTED_AT -> LibraryEntrySortEnum.STARTED_AT
    LibraryFilterOptions.SortBy.UPDATED_AT -> LibraryEntrySortEnum.UPDATED_AT
    LibraryFilterOptions.SortBy.PROGRESS -> LibraryEntrySortEnum.PROGRESS
    LibraryFilterOptions.SortBy.RATING -> LibraryEntrySortEnum.RATING
}

fun LibraryFilterOptions.SortDirection.toSortDirection() = when (this) {
    LibraryFilterOptions.SortDirection.ASC -> SortDirection.ASCENDING
    LibraryFilterOptions.SortDirection.DESC -> SortDirection.DESCENDING
}