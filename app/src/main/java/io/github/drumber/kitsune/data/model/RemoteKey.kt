package io.github.drumber.kitsune.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val resourceId: String,
    val remoteKeyType: RemoteKeyType,
    val prevPageKey: Int?,
    val nextPageKey: Int?
)

enum class RemoteKeyType {
    Anime, Manga, LibraryEntry
}
