package io.github.drumber.kitsune.data.model.mediarelationship

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.resource.Media
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("mediaRelationships")
data class MediaRelationship(
    @Id val id: String?,
    val role: RelationshipRole?,
    @Relationship("destination")
    val resource: Media?
) : Parcelable

/**
 * Media relationship roles, sort order from
 * https://github.com/hummingbird-me/kitsu-server/blob/the-future/app/models/media_relationship.rb
 */
enum class RelationshipRole {
    @JsonProperty("sequel")
    Sequel,
    @JsonProperty("prequel")
    Prequel,
    @JsonProperty("alternative_setting")
    AlternativeSetting,
    @JsonProperty("alternative_version")
    AlternativeVersion,
    @JsonProperty("side_story")
    SideStory,
    @JsonProperty("parent_story")
    ParentStory,
    @JsonProperty("summary")
    Summary,
    @JsonProperty("full_story")
    FullStory,
    @JsonProperty("spinoff")
    Spinoff,
    @JsonProperty("adaptation")
    Adaptation,
    @JsonProperty("character")
    Character,
    @JsonProperty("other")
    Other
}
