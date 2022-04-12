package io.github.drumber.kitsune.data.model.mediarelationship

import android.content.Context
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.Media
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("mediaRelationships")
data class MediaRelationship(
    @Id val id: String?,
    val role: RelationshipRole?,
    @Relationship("destination")
    val media: Media?
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

fun RelationshipRole.getString(context: Context): String {
    val stringRes = when (this) {
        RelationshipRole.Sequel -> R.string.relationship_sequel
        RelationshipRole.Prequel -> R.string.relationship_prequel
        RelationshipRole.AlternativeSetting -> R.string.relationship_alternative_setting
        RelationshipRole.AlternativeVersion -> R.string.relationship_alternative_version
        RelationshipRole.SideStory -> R.string.relationship_side_story
        RelationshipRole.ParentStory -> R.string.relationship_parent_story
        RelationshipRole.Summary -> R.string.relationship_summary
        RelationshipRole.FullStory -> R.string.relationship_full_story
        RelationshipRole.Spinoff -> R.string.relationship_spinoff
        RelationshipRole.Adaptation -> R.string.relationship_adaptation
        RelationshipRole.Character -> R.string.relationship_character
        RelationshipRole.Other -> R.string.relationship_other
    }
    return context.getString(stringRes)
}
