package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.Relation

data class LocalLibraryEntryWithModificationAndNextMediaUnit(
    @Embedded
    val libraryEntry: LocalLibraryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val libraryEntryModification: LocalLibraryEntryModification?,
    @Relation(
        parentColumn = "id",
        entityColumn = "libraryEntryId"
    )
    val nextMediaUnit: LocalNextMediaUnit?
)
