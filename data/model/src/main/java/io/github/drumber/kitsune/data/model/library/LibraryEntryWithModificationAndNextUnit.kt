package io.github.drumber.kitsune.data.model.library

import io.github.drumber.kitsune.data.model.media.unit.MediaUnit

data class LibraryEntryWithModificationAndNextUnit(
    val libraryEntryWithModification: LibraryEntryWithModification,
    val nextUnit: MediaUnit?
)
