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
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    /**  Set to `-1` to remove rating (will be mapped to `null` by the json serializer) */
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?
) : Parcelable
