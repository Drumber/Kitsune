package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = LocalLibraryEntry::class,
            parentColumns = ["id"],
            childColumns = ["resourceId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class RemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val resourceId: String,
    val remoteKeyType: RemoteKeyType,
    val prevPageKey: String?,
    val nextPageKey: String?
)

enum class RemoteKeyType {
    LibraryEntry,
    LibraryEntryWithNextMediaUnit
}
