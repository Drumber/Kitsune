package io.github.drumber.kitsune.data.model.media.relationship

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