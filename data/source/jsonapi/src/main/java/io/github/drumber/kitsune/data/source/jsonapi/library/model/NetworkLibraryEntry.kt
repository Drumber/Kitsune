package io.github.drumber.kitsune.data.source.jsonapi.library.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.media.MediaType
import io.github.drumber.kitsune.data.source.jsonapi.NullableIntSerializer
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser

@Type("libraryEntries")
data class NetworkLibraryEntry(
    @Id
    val id: String?,
    val updatedAt: String?,

    val startedAt: String?,
    val finishedAt: String?,
    val progressedAt: String?,

    val status: NetworkLibraryStatus?,
    val progress: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    /** set ratingTwenty to '-1' to serialize to 'null' */
    @JsonSerialize(using = NullableIntSerializer::class)
    val ratingTwenty: Int?,

    val notes: String?,
    @JsonProperty("private")
    val privateEntry: Boolean?,
    val reactionSkipped: NetworkReactionSkip?,

    @Relationship("anime")
    val anime: NetworkAnime?,
    @Relationship("manga")
    val manga: NetworkManga?,
    @Relationship("user")
    val user: NetworkUser?
) {

    companion object {
        fun new(
            userId: String,
            mediaType: MediaType,
            mediaId: String,
            status: NetworkLibraryStatus? = null
        ) = NetworkLibraryEntry(
            user = NetworkUser(id = userId),
            status = status,
            anime = mediaType.takeIf { it == MediaType.Anime }?.let { NetworkAnime.empty(mediaId) },
            manga = mediaType.takeIf { it == MediaType.Manga }?.let { NetworkManga.empty(mediaId) },
            id = null,
            updatedAt = null,
            startedAt = null,
            finishedAt = null,
            progressedAt = null,
            progress = null,
            reconsuming = null,
            reconsumeCount = null,
            volumesOwned = null,
            ratingTwenty = null,
            notes = null,
            privateEntry = null,
            reactionSkipped = null
        )

        fun update(
            id: String,
            startedAt: String? = null,
            finishedAt: String? = null,
            status: NetworkLibraryStatus? = null,
            progress: Int? = null,
            reconsumeCount: Int? = null,
            volumesOwned: Int? = null,
            ratingTwenty: Int? = null,
            notes: String? = null,
            isPrivate: Boolean? = null
        ) = NetworkLibraryEntry(
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
            privateEntry = isPrivate,
            reactionSkipped = null,
            anime = null,
            manga = null,
            user = null
        )
    }

}
