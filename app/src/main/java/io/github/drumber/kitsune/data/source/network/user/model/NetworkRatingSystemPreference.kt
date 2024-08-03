package io.github.drumber.kitsune.data.source.network.user.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkRatingSystemPreference {
    // 0.5, 1...10
    @JsonProperty("advanced")
    Advanced,
    // 0.5, 1...5
    @JsonProperty("regular")
    Regular,
    // :(, :|, :), :D
    @JsonProperty("simple")
    Simple
}
