package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState.NOT_SYNCHRONIZED

@Entity(tableName = "library_entries_modifications")
data class LocalLibraryEntryModification(
    /** Corresponds to the library entry ID */
    @PrimaryKey val id: String,

    val createTime: Long = System.currentTimeMillis(),
    val state: LocalLibraryModificationState = NOT_SYNCHRONIZED,

    val startedAt: String?,
    val finishedAt: String?,

    val status: LocalLibraryStatus?,
    val progress: Int?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    /**  Set to `-1` to remove rating (will be mapped to `null` by the json serializer) */
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?
) {

    fun isEqualToLibraryEntry(localLibraryEntry: LocalLibraryEntry): Boolean {
        return id == localLibraryEntry.id && applyToLibraryEntry(localLibraryEntry) == localLibraryEntry
    }

    fun applyToLibraryEntry(localLibraryEntry: LocalLibraryEntry): LocalLibraryEntry {
        require(id == localLibraryEntry.id) { "ID of the library modification and the entry must be the same." }
        return localLibraryEntry.copy(
            startedAt = startedAt ?: localLibraryEntry.startedAt,
            finishedAt = finishedAt ?: localLibraryEntry.finishedAt,
            status = status ?: localLibraryEntry.status,
            progress = progress ?: localLibraryEntry.progress,
            reconsumeCount = reconsumeCount ?: localLibraryEntry.reconsumeCount,
            volumesOwned = volumesOwned ?: localLibraryEntry.volumesOwned,
            ratingTwenty = ratingTwenty ?: localLibraryEntry.ratingTwenty,
            notes = notes?.takeIf { it.isNotBlank() || !localLibraryEntry.notes.isNullOrBlank() }
                ?: localLibraryEntry.notes,
            privateEntry = privateEntry ?: localLibraryEntry.privateEntry
        )
    }
}
