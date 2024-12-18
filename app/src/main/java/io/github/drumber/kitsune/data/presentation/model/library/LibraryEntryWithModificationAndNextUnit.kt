package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit

data class LibraryEntryWithModificationAndNextUnit(
    val libraryEntryWithModification: LibraryEntryWithModification,
    val nextUnit: MediaUnit?
)
