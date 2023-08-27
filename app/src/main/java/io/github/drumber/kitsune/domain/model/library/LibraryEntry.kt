package io.github.drumber.kitsune.domain.model.library

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.library.ReactionSkip
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.model.media.Anime
import io.github.drumber.kitsune.domain.model.media.Manga
import io.github.drumber.kitsune.util.network.EmptyStringIdHandler
import io.github.drumber.kitsune.util.network.NullableIntSerializer
import kotlinx.parcelize.Parcelize

// TODO: delete this
@Deprecated("Replaced with new model.")
@Parcelize
//@Entity(tableName = "library_table")
@Type("libraryEntries")
data class LibraryEntry(
    @PrimaryKey @Id(EmptyStringIdHandler::class)
    var id: String = "",
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var status: LibraryStatus? = null,
    var progress: Int? = null,
    var volumesOwned: Int? = null,
    var reconsuming: Boolean? = null,
    var reconsumeCount: Int? = null,
    var notes: String? = null,
    @JsonProperty("private")
    var privateEntry: Boolean? = null,
    var reactionSkipped: ReactionSkip? = null,
    var progressedAt: String? = null,
    var startedAt: String? = null,
    var finishedAt: String? = null,
    @JsonSerialize(using = NullableIntSerializer::class)
    var ratingTwenty: Int? = null, // set to '-1' to serialize to 'null'
    @Embedded(prefix = "anime_")
    @Relationship("anime")
    var anime: Anime? = null,
    @Embedded(prefix = "manga_")
    @Relationship("manga")
    var manga: Manga? = null,
    @Ignore
    @Relationship("user")
    var user: User? = null
): Parcelable
