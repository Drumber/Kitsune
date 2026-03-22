package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "remote_keys",
    primaryKeys = ["resourceId", "requestType"],
    foreignKeys = [
        ForeignKey(
            entity = LocalLibraryEntry::class,
            parentColumns = ["id"],
            childColumns = ["resourceId"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class RemoteKeyEntity(
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val resourceId: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val requestType: String,
    val prevPageKey: String?,
    val nextPageKey: String?,
    @ColumnInfo(defaultValue = "0")
    val stale: Boolean = false,
) {
    constructor(
        resourceId: String,
        filterOptions: LocalLibraryFilterOptions,
        prevPageKey: String?,
        nextPageKey: String?
    ) : this(
        resourceId = resourceId,
        requestType = filterOptions.serialize(),
        prevPageKey = prevPageKey,
        nextPageKey = nextPageKey
    )
}
