package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit

data class LibraryEntryWithNextUnit(
    val libraryEntry: LibraryEntry,
    val nextUnit: MediaUnit?
)
