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
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val status: String? = null,
    val progress: Int? = null,
    val volumesOwned: Int? = null,
    val reconsuming: Boolean? = null,
    val reconsumeCount: Int? = null,
    val notes: String? = null,
    val private: Boolean? = null,
    val reactionSkipped: ReactionSkip? = null,
    val progressedAt: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val ratingTwenty: Int? = null,
    @Relationship("anime")
    val anime: Anime? = null,
    @Relationship("manga")
    val manga: Manga? = null
): Parcelable

enum class ReactionSkip {
    Unskipped,
    Skipped,
    Ignored
}
