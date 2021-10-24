package io.github.drumber.kitsune.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val queryOptions: String,
    val prevPageKey: Int?,
    val nextPageKey: Int?
)
