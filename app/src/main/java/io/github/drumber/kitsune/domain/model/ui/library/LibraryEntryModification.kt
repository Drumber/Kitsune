package io.github.drumber.kitsune.domain.model.ui.library

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibraryEntryModification(
    /** Corresponds to the library entry ID */
    val id: String,

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
        fun withIdAndNulls(id: String) = LibraryEntryModification(
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
        libraryEntry: LibraryEntry, ignoreBlankNotes: Boolean = true
    ): LibraryEntry {
        return libraryEntry.copy(
            startedAt = startedAt ?: libraryEntry.startedAt,
            finishedAt = finishedAt ?: libraryEntry.finishedAt,
            status = status ?: libraryEntry.status,
            progress = progress ?: libraryEntry.progress,
            reconsumeCount = reconsumeCount ?: libraryEntry.reconsumeCount,
            volumesOwned = volumesOwned ?: libraryEntry.volumesOwned,
            ratingTwenty = ratingTwenty ?: libraryEntry.ratingTwenty,
            notes = notes
                ?.takeIf { !ignoreBlankNotes || libraryEntry.notes != null || it.isNotBlank() }
                ?: libraryEntry.notes,
            privateEntry = privateEntry ?: libraryEntry.privateEntry
        )
    }

    fun isEqualToLibraryEntry(libraryEntry: LibraryEntry): Boolean {
        return libraryEntry == applyToLibraryEntry(libraryEntry)
    }

}
