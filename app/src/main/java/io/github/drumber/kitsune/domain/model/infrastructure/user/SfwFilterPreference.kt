package io.github.drumber.kitsune.domain.model.infrastructure.user

import com.fasterxml.jackson.annotation.JsonProperty

enum class SfwFilterPreference {
    @JsonProperty("sfw")
    SFW,
    @JsonProperty("nsfw_sometimes")
    NSFW_SOMETIMES,
    @JsonProperty("nsfw_everywhere")
    NSFW_EVERYWHERE
}
