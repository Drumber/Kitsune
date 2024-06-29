package io.github.drumber.kitsune.domain_old.model.ui.media

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.mediarelationship.MediaRelationshipRole

fun MediaRelationshipRole.getString(context: Context): String {
    val stringRes = when (this) {
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
    return context.getString(stringRes)
}
