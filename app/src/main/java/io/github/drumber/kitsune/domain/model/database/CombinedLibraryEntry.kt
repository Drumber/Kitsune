package io.github.drumber.kitsune.domain.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class CombinedLibraryEntry(
    @Embedded
    val libraryEntry: LocalLibraryEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val libraryEntryModification: LocalLibraryEntryModification?,

    @Relation(
        parentColumn = "animeId",
        entityColumn = "id"
    )
    val anime: LocalAnime,

    @Relation(
        parentColumn = "mangaId",
        entityColumn = "id"
    )
    val manga: LocalManga
)
