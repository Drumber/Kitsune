package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithNextUnit
import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.source.graphql.fragment.LibraryEntryWithNextUnitFragment
import io.github.drumber.kitsune.data.source.graphql.mapper.toTitles

fun LibraryEntryWithNextUnitFragment.toLibraryEntryWithNextUnit() = LibraryEntryWithNextUnit(
    libraryEntry = libraryEntryFragment.toLibraryEntry(),
    nextUnit = nextUnit?.toMediaUnit(libraryEntryFragment.media.libraryMediaFragment.type)
)

fun LibraryEntryWithNextUnitFragment.NextUnit.toMediaUnit(mediaType: String) =
    when (mediaType) {
        "Anime" -> toEpisode()
        "Manga" -> toChapter()
        else -> null
    }

fun LibraryEntryWithNextUnitFragment.NextUnit.toEpisode() = Episode(
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

fun LibraryEntryWithNextUnitFragment.NextUnit.toChapter() = Chapter(
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