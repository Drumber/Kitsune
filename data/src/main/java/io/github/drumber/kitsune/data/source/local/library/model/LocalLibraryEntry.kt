package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_entries")
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
