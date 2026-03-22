package io.github.drumber.kitsune.data.source.jsonapi.character.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkMediaCharacterRole {
    @JsonProperty("main")
    MAIN,
    @JsonProperty("supporting")
    SUPPORTING,
    @JsonProperty("recurring")
    RECURRING,
    @JsonProperty("cameo")
    CAMEO
}
