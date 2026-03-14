package io.github.drumber.kitsune.data.model.library

import io.github.drumber.kitsune.data.model.media.MediaType

enum class LibraryEntryMediaType {
    All,
    Anime,
    Manga
}

fun LibraryEntryMediaType.toMediaType() = when (this) {
    LibraryEntryMediaType.Anime -> MediaType.Anime
    LibraryEntryMediaType.Manga -> MediaType.Manga
    LibraryEntryMediaType.All -> null
}