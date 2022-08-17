package io.github.drumber.kitsune.data.model.library

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.drumber.kitsune.util.network.NullableIntSerializer
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "offline_library_modification")
data class LibraryModification(
    /** Corresponds to the library entry ID */
    @PrimaryKey val id: String,
    val status: Status? = null,
    val progress: Int? = null,
    val volumesOwned: Int? = null,
    val reconsumeCount: Int? = null,
    val notes: String? = null,
    @JsonProperty("private")
    val isPrivate: Boolean? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    @JsonSerialize(using = NullableIntSerializer::class)
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
        isPrivate?.let { libraryEntry.isPrivate = it }
        startedAt?.let { libraryEntry.startedAt = it }
        finishedAt?.let { libraryEntry.finishedAt = it }
        ratingTwenty?.let { libraryEntry.ratingTwenty = it }

        return libraryEntry
    }

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
