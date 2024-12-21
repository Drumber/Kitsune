package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesWithNextUnitQuery

fun GetLibraryEntriesWithNextUnitQuery.All.toLibraryEntriesWithModificationAndNextUnit() = nodes
    ?.filterNotNull()
    ?.map(GetLibraryEntriesWithNextUnitQuery.Node::toLibraryEntriesWithModificationAndNextUnit)

fun GetLibraryEntriesWithNextUnitQuery.Node.toLibraryEntriesWithModificationAndNextUnit() =
    LibraryEntryWithModificationAndNextUnit(
        LibraryEntryWithModification(
            libraryEntryFragment.toLibraryEntry(),
            null
        ),
        nextUnit?.toMediaUnit(libraryEntryFragment.media.type)
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
    titles = titles.titlesFragment.toTitles(),
    canonicalTitle = titles.titlesFragment.canonical,
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
    titles = titles.titlesFragment.toTitles(),
    canonicalTitle = titles.titlesFragment.canonical,
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
