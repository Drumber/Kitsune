package io.github.drumber.kitsune.data.source.network.media.model.relationship

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Media relationship roles, sort order from
 * https://github.com/hummingbird-me/kitsu-server/blob/the-future/app/models/media_relationship.rb
 */
enum class NetworkMediaRelationshipRole {
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