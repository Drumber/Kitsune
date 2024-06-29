package io.github.drumber.kitsune.data.source.network.user.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkSfwFilterPreference {
    @JsonProperty("sfw")
    SFW,
    @JsonProperty("nsfw_sometimes")
    NSFW_SOMETIMES,
    @JsonProperty("nsfw_everywhere")
    NSFW_EVERYWHERE
}
