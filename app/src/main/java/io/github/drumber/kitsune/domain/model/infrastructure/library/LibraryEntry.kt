package io.github.drumber.kitsune.domain.model.infrastructure.library

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.model.media.Anime
import io.github.drumber.kitsune.domain.model.media.Manga
import io.github.drumber.kitsune.util.network.NullableIntSerializer
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("libraryEntries")
data class LibraryEntry(
    @Id
    val id: String,
    val createdAt: String?,
    val updatedAt: String?,

    val startedAt: String?,
    val finishedAt: String?,
    val progressedAt: String?,

    val status: LibraryStatus?,
    val progress: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    @JsonSerialize(using = NullableIntSerializer::class)
    val ratingTwenty: Int?, // set to '-1' to serialize to 'null'

    val notes: String?,
    @JsonProperty("private")
    val privateEntry: Boolean?,
    val reactionSkipped: ReactionSkip?,

    @Relationship("anime")
    val anime: Anime?,
    @Relationship("manga")
    val manga: Manga?,
    @Relationship("user")
    val user: User?
): Parcelable
