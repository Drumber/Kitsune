package io.github.drumber.kitsune.domain.model.infrastructure.user

import com.fasterxml.jackson.annotation.JsonProperty

enum class RatingSystemPreference {
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
