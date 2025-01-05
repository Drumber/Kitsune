package io.github.drumber.kitsune.data.source.graphql.mapper

import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.model.media.AgeRating
import io.github.drumber.kitsune.data.common.model.media.AnimeSubtype
import io.github.drumber.kitsune.data.common.model.media.MangaSubtype
import io.github.drumber.kitsune.data.common.model.media.MediaType
import io.github.drumber.kitsune.data.common.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.source.graphql.type.AgeRatingEnum
import io.github.drumber.kitsune.data.source.graphql.type.AnimeSubtypeEnum
import io.github.drumber.kitsune.data.source.graphql.type.LibraryEntrySortEnum
import io.github.drumber.kitsune.data.source.graphql.type.MangaSubtypeEnum
import io.github.drumber.kitsune.data.source.graphql.type.MediaTypeEnum
import io.github.drumber.kitsune.data.source.graphql.type.ReleaseStatusEnum
import io.github.drumber.kitsune.data.source.graphql.type.SortDirection

fun MediaType.toMediaTypeEnum() = when (this) {
    MediaType.Anime -> MediaTypeEnum.ANIME
    MediaType.Manga -> MediaTypeEnum.MANGA
}

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

fun AgeRatingEnum.toAgeRating() = when (this) {
    AgeRatingEnum.G -> AgeRating.G
    AgeRatingEnum.PG -> AgeRating.PG
    AgeRatingEnum.R -> AgeRating.R
    AgeRatingEnum.R18 -> AgeRating.R18
    AgeRatingEnum.UNKNOWN__ -> null
}