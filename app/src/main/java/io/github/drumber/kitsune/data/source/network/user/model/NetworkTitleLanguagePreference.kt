package io.github.drumber.kitsune.data.source.network.user.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkTitleLanguagePreference {
    @JsonProperty("canonical")
    Canonical,
    @JsonProperty("romanized")
    Romanized,
    @JsonProperty("english")
    English
}