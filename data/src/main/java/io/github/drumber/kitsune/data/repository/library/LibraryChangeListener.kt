package io.github.drumber.kitsune.data.repository.library

import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryModification

interface LibraryChangeListener {
    fun onNewLibraryEntry(libraryEntry: LibraryEntry)
    fun onUpdateLibraryEntry(
        libraryEntryModification: LibraryEntryModification,
        updatedLibraryEntry: LibraryEntry?
    )

    fun onRemoveLibraryEntry(id: String)
    fun onDataInsertion(libraryEntries: List<LibraryEntry>)
}
