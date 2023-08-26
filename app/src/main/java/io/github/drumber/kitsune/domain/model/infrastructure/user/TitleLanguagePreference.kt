package io.github.drumber.kitsune.domain.model.infrastructure.user

import com.fasterxml.jackson.annotation.JsonProperty

enum class TitleLanguagePreference {
    @JsonProperty("canonical")
    Canonical,
    @JsonProperty("romanized")
    Romanized,
    @JsonProperty("english")
    English
}