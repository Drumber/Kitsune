package io.github.drumber.kitsune.data.common.mapper

import io.github.drumber.kitsune.data.common.library.LibraryEntryMediaType
import io.github.drumber.kitsune.data.common.media.MediaType

fun LibraryEntryMediaType.toMediaType() = when (this) {
    LibraryEntryMediaType.Anime -> MediaType.Anime
    LibraryEntryMediaType.Manga -> MediaType.Manga
    LibraryEntryMediaType.All -> null
}