package io.github.drumber.kitsune.data.model.library

import io.github.drumber.kitsune.data.model.media.unit.MediaUnit

data class LibraryEntryWithNextUnit(
    val libraryEntry: LibraryEntry,
    val nextUnit: MediaUnit?
)
