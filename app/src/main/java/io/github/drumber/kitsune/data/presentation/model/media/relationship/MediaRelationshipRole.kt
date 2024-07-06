package io.github.drumber.kitsune.data.presentation.model.media.relationship

import androidx.annotation.StringRes
import io.github.drumber.kitsune.R

/**
 * Media relationship roles, sort order from
 * https://github.com/hummingbird-me/kitsu-server/blob/the-future/app/models/media_relationship.rb
 */
enum class MediaRelationshipRole {
    Sequel,
    Prequel,
    AlternativeSetting,
    AlternativeVersion,
    SideStory,
    ParentStory,
    Summary,
    FullStory,
    Spinoff,
    Adaptation,
    Character,
    Other
}

@StringRes
fun MediaRelationshipRole.getStringRes(): Int {
    return when (this) {
        MediaRelationshipRole.Sequel -> R.string.relationship_sequel
        MediaRelationshipRole.Prequel -> R.string.relationship_prequel
        MediaRelationshipRole.AlternativeSetting -> R.string.relationship_alternative_setting
        MediaRelationshipRole.AlternativeVersion -> R.string.relationship_alternative_version
        MediaRelationshipRole.SideStory -> R.string.relationship_side_story
        MediaRelationshipRole.ParentStory -> R.string.relationship_parent_story
        MediaRelationshipRole.Summary -> R.string.relationship_summary
        MediaRelationshipRole.FullStory -> R.string.relationship_full_story
        MediaRelationshipRole.Spinoff -> R.string.relationship_spinoff
        MediaRelationshipRole.Adaptation -> R.string.relationship_adaptation
        MediaRelationshipRole.Character -> R.string.relationship_character
        MediaRelationshipRole.Other -> R.string.relationship_other
    }
}
