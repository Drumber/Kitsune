package io.github.drumber.kitsune.data.common

import io.github.drumber.kitsune.data.model.library.LibraryEntryMediaType
import io.github.drumber.kitsune.data.model.media.MediaType

fun LibraryEntryMediaType.toMediaType() = when (this) {
    LibraryEntryMediaType.Anime -> MediaType.Anime
    LibraryEntryMediaType.Manga -> MediaType.Manga
    LibraryEntryMediaType.All -> null
}