package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.data.common.Titles

@Entity(
    tableName = "next_media_units",
    foreignKeys = [
        ForeignKey(
            entity = LocalLibraryEntry::class,
            parentColumns = ["id"],
            childColumns = ["libraryEntryId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class LocalNextMediaUnit(
    @PrimaryKey
    val libraryEntryId: String,
    val unitId: String,
    val titles: Titles?,
    val canonicalTitle: String?,
    val number: Int?,
    @Embedded(prefix = "thumbnail_")
    val thumbnail: LocalImage?
)
