package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val resourceId: String,
    val remoteKeyType: RemoteKeyType,
    val prevPageKey: Int?,
    val nextPageKey: Int?
)

enum class RemoteKeyType {
    LibraryEntry
}
