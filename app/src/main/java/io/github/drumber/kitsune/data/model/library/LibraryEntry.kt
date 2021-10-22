package io.github.drumber.kitsune.data.model.library

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("libraryEntries")
data class LibraryEntry(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val status: String?,
    val progress: Int?,
    val volumesOwned: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val notes: String?,
    val private: Boolean?,
    val reactionSkipped: ReactionSkip?,
    val progressedAt: String?,
    val startedAt: String?,
    val finishedAt: String?,
    val ratingTwenty: Int?,
    @Relationship("anime")
    val anime: Anime?,
    @Relationship("manga")
    val manga: Manga?
): Parcelable

enum class ReactionSkip {
    Unskipped,
    Skipped,
    Ignored
}
