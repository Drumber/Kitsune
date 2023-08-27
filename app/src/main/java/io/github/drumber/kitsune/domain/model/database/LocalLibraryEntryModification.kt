package io.github.drumber.kitsune.domain.model.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "library_entries_modifications")
data class LocalLibraryEntryModification(
    /** Corresponds to the library entry ID */
    @PrimaryKey val id: String,

    val startedAt: String?,
    val finishedAt: String?,

    val status: LibraryStatus?,
    val progress: Int?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    /**  Set to `-1` to remove rating (will be mapped to `null` by the json serializer) */
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?
) : Parcelable {

    companion object {
        fun withIdAndNulls(id: String) = LocalLibraryEntryModification(
            id = id,
            startedAt = null,
            finishedAt = null,
            status = null,
            progress = null,
            reconsumeCount = null,
            volumesOwned = null,
            ratingTwenty = null,
            notes = null,
            privateEntry = null
        )
    }

    /**
     * Apply modifications to the specified library entry.
     *
     * @param ignoreBlankNotes
     * Do not apply notes if modified notes is blank and library entry notes is null.
     *
     * @return the given library entry for convenience.
     */
    fun applyToLibraryEntry(
        libraryEntry: LocalLibraryEntry, ignoreBlankNotes: Boolean = true
    ): LocalLibraryEntry {
//        status?.let { libraryEntry.status = it }
//        progress?.let { libraryEntry.progress = it }
//        volumesOwned?.let { libraryEntry.volumesOwned = it }
//        reconsumeCount?.let { libraryEntry.reconsumeCount = it }
//        notes?.let {
//            if (!ignoreBlankNotes || libraryEntry.notes != null || it.isNotBlank()) {
//                libraryEntry.notes = it
//            }
//        }
//        isPrivate?.let { libraryEntry.privateEntry = it }
//        startedAt?.let { libraryEntry.startedAt = it }
//        finishedAt?.let { libraryEntry.finishedAt = it }
//        ratingTwenty?.let { libraryEntry.ratingTwenty = it }

        return libraryEntry
    }

    fun mergeModificationFrom(other: LocalLibraryEntryModification) = LocalLibraryEntryModification(
        id = id,
        status = other.status ?: status,
        progress = other.progress ?: progress,
        volumesOwned = other.volumesOwned ?: volumesOwned,
        reconsumeCount = other.reconsumeCount ?: reconsumeCount,
        notes = other.notes ?: notes,
        privateEntry = other.privateEntry ?: privateEntry,
        startedAt = other.startedAt ?: startedAt,
        finishedAt = other.finishedAt ?: finishedAt,
        ratingTwenty = other.ratingTwenty ?: ratingTwenty
    )

    fun toLocalLibraryEntry(ignoreBlankNotes: Boolean = true): LocalLibraryEntry {
        // TODO: ignore blank notes
        return LocalLibraryEntry(
            id = id,
            updatedAt = null,
            startedAt = startedAt,
            finishedAt = finishedAt,
            progressedAt = null,
            status = status,
            progress = progress,
            reconsuming = null,
            reconsumeCount = reconsumeCount,
            volumesOwned = volumesOwned,
            ratingTwenty = ratingTwenty,
            notes = notes,
            privateEntry = privateEntry,
            reactionSkipped = null,
            anime = null,
            manga = null
        )
    }

    fun isEqualToLibraryEntry(libraryEntry: LocalLibraryEntry): Boolean {
        val tempEntry = libraryEntry.copy()
        applyToLibraryEntry(tempEntry)
        return tempEntry == libraryEntry
    }

}
