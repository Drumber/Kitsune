package io.github.drumber.kitsune.domain.model.infrastructure.user

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.drumber.kitsune.R

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

fun RatingSystemPreference.getStringRes() = when (this) {
    RatingSystemPreference.Simple -> R.string.preference_rating_system_simple
    RatingSystemPreference.Regular -> R.string.preference_rating_system_regular
    RatingSystemPreference.Advanced -> R.string.preference_rating_system_advanced
}
