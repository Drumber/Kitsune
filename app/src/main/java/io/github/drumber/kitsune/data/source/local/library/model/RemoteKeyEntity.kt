package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "remote_keys",
    primaryKeys = ["resourceId", "remoteKeyType"],
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
