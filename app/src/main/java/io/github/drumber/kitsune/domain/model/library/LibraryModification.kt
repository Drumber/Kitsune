package io.github.drumber.kitsune.domain.model.library

import android.os.Parcelable
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import kotlinx.parcelize.Parcelize

// TODO: delete this
@Deprecated("Replaced with new model.")
@Parcelize
//@Entity(tableName = "offline_library_modification")
data class LibraryModification(
    /** Corresponds to the library entry ID */
    @PrimaryKey val id: String,
    val status: LibraryStatus? = null,
    val progress: Int? = null,
    val volumesOwned: Int? = null,
    val reconsumeCount: Int? = null,
    val notes: String? = null,
    val isPrivate: Boolean? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    /** Set to `-1` to remove rating (will be mapped to `null` by the json serializer). */
    val ratingTwenty: Int? = null
) : Parcelable {

    /**
     * Apply modifications to the specified library entry.
     *
     * @param ignoreBlankNotes
     * Do not apply notes if modified notes is blank and library entry notes is null.
     *
     * @return the given library entry for convenience.
     */
    fun applyToLibraryEntry(libraryEntry: LibraryEntry, ignoreBlankNotes: Boolean = true): LibraryEntry {
        status?.let { libraryEntry.status = it }
        progress?.let { libraryEntry.progress = it }
        volumesOwned?.let { libraryEntry.volumesOwned = it }
        reconsumeCount?.let { libraryEntry.reconsumeCount = it }
        notes?.let {
            if (!ignoreBlankNotes || libraryEntry.notes != null || it.isNotBlank()) {
                libraryEntry.notes = it
            }
        }
        isPrivate?.let { libraryEntry.privateEntry = it }
        startedAt?.let { libraryEntry.startedAt = it }
        finishedAt?.let { libraryEntry.finishedAt = it }
        ratingTwenty?.let { libraryEntry.ratingTwenty = it }

        return libraryEntry
    }

    fun mergeModificationFrom(other: LibraryModification) = LibraryModification(
        id = id,
        status = other.status ?: status,
        progress = other.progress ?: progress,
        volumesOwned = other.volumesOwned ?: volumesOwned,
        reconsumeCount = other.reconsumeCount ?: reconsumeCount,
        notes = other.notes ?: notes,
        isPrivate = other.isPrivate ?: isPrivate,
        startedAt = other.startedAt ?: startedAt,
        finishedAt = other.finishedAt ?: finishedAt,
        ratingTwenty = other.ratingTwenty ?: ratingTwenty
    )

    fun toLibraryEntry(ignoreBlankNotes: Boolean = true): LibraryEntry {
        val newEntry = LibraryEntry(id = id)
        applyToLibraryEntry(newEntry, ignoreBlankNotes)
        return newEntry
    }

    fun isEqualToLibraryEntry(libraryEntry: LibraryEntry): Boolean {
        val tempEntry = libraryEntry.copy()
        applyToLibraryEntry(tempEntry)
        return tempEntry == libraryEntry
    }

}
