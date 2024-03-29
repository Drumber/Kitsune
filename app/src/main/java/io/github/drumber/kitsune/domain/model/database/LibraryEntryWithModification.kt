package io.github.drumber.kitsune.domain.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class LibraryEntryWithModification(
    @Embedded
    val libraryEntry: LocalLibraryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val libraryEntryModification: LocalLibraryEntryModification?
)
