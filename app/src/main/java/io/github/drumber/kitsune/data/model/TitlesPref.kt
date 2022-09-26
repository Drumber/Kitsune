package io.github.drumber.kitsune.data.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class TitlesPref {
    @JsonProperty("canonical")
    Canonical,
    @JsonProperty("romanized")
    Romanized,
    @JsonProperty("english")
    English
}