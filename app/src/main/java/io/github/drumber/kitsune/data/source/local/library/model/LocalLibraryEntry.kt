package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "library_entries",
    indices = [
        // Serves the combined `status IN (...) AND media_type = ...` filter as well as
        // status-only filtering and the `ORDER BY status` clause (leftmost prefix).
        Index(value = ["status", "media_type"]),
        // Serves the `media_type = ...` only filters.
        Index(value = ["media_type"]),
        // Serves the frequent point lookups by media id (e.g. media detail screen).
        Index(value = ["media_id"])
    ]
)
data class LocalLibraryEntry(
    @PrimaryKey
    val id: String,
    val updatedAt: String?,

    val startedAt: String?,
    val finishedAt: String?,
    val progressedAt: String?,

    val status: LocalLibraryStatus?,
    val progress: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?,
    val reactionSkipped: LocalReactionSkip?,

    @Embedded("media_")
    val media: LocalLibraryMedia?
)
