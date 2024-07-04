package io.github.drumber.kitsune.data.presentation.model.library

data class LibraryEntryWithModification(
    val libraryEntry: LibraryEntry,
    val modification: LibraryEntryModification?
)
