package io.github.drumber.kitsune.data.model.library

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.drumber.kitsune.util.network.NullableIntSerializer
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName =  "offline_library_update")
data class OfflineLibraryUpdate(
    @PrimaryKey val id: String,
    val status: Status? = null,
    val progress: Int? = null,
    @JsonSerialize(using = NullableIntSerializer::class)
    val ratingTwenty: Int? = null
) : Parcelable {

    fun toLibraryEntry() = LibraryEntry(
        id = id,
        status = status,
        progress = progress,
        ratingTwenty = ratingTwenty
    )

    fun isEqualToLibraryEntry(libraryEntry: LibraryEntry): Boolean {
        return libraryEntry.id.equalOrNull(id)
                && libraryEntry.status.equalOrNull(status)
                && libraryEntry.progress.equalOrNull(progress)
                && libraryEntry.ratingTwenty.equalOrNull(ratingTwenty)
    }

    private fun Any?.equalOrNull(that: Any?) = this@equalOrNull == that || that == null

}

fun LibraryEntry.toOfflineLibraryUpdate() = OfflineLibraryUpdate(
    id = id,
    status = status,
    progress = progress,
    ratingTwenty = ratingTwenty
)
