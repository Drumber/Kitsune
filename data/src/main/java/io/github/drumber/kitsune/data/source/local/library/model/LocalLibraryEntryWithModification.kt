package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.Relation

data class LocalLibraryEntryWithModification(
    @Embedded
    val libraryEntry: LocalLibraryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val libraryEntryModification: LocalLibraryEntryModification?
)
