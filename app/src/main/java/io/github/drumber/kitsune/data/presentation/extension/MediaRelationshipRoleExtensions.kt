package io.github.drumber.kitsune.data.presentation.extension

import androidx.annotation.StringRes
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.relationship.MediaRelationshipRole

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
