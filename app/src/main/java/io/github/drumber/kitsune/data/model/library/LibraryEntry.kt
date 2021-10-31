package io.github.drumber.kitsune.data.model.library

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "library_table")
@Type("libraryEntries")
data class LibraryEntry(
    @PrimaryKey @Id
    var id: String = "",
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var status: Status? = null,
    var progress: Int? = null,
    var volumesOwned: Int? = null,
    var reconsuming: Boolean? = null,
    var reconsumeCount: Int? = null,
    var notes: String? = null,
    var private: Boolean? = null,
    var reactionSkipped: ReactionSkip? = null,
    var progressedAt: String? = null,
    var startedAt: String? = null,
    var finishedAt: String? = null,
    var ratingTwenty: Int? = null,
    @Embedded(prefix = "anime_")
    @Relationship("anime")
    var anime: Anime? = null,
    @Embedded(prefix = "manga_")
    @Relationship("manga")
    var manga: Manga? = null
): Parcelable

enum class Status {
    Completed,
    Current,
    Dropped,
    @JsonProperty("on_hold") OnHold,
    Planned
}

enum class ReactionSkip {
    Unskipped,
    Skipped,
    Ignored
}
