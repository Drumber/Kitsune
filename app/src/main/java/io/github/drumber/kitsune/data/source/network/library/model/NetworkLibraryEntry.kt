package io.github.drumber.kitsune.data.source.network.library.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.util.json.NullableIntSerializer

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
)
