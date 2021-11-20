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

enum class RelationshipRole {
    @JsonProperty("adaptation")
    Adaptation,
    @JsonProperty("alternative_setting")
    AlternativeSetting,
    @JsonProperty("alternative_version")
    AlternativeVersion,
    @JsonProperty("character")
    Character,
    @JsonProperty("full_story")
    FullStory,
    @JsonProperty("other")
    Other,
    @JsonProperty("parent_story")
    ParentStory,
    @JsonProperty("prequel")
    Prequel,
    @JsonProperty("sequel")
    Sequel,
    @JsonProperty("side_story")
    SideStory,
    @JsonProperty("spinoff")
    Spinoff,
    @JsonProperty("summary")
    Summary
}
