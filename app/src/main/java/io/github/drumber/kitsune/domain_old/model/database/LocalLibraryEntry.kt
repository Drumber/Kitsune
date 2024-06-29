package io.github.drumber.kitsune.domain_old.model.database

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain_old.model.common.library.ReactionSkip
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "library_entries")
data class LocalLibraryEntry(
    @PrimaryKey
    val id: String,
    val updatedAt: String?,

    val startedAt: String?,
    val finishedAt: String?,
    val progressedAt: String?,

    val status: LibraryStatus?,
    val progress: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?,
    val reactionSkipped: ReactionSkip?,

    @Embedded("anime_")
    val anime: LocalAnime?,
    @Embedded("manga_")
    val manga: LocalManga?
) : Parcelable
