package io.github.drumber.kitsune.domain_old.model.infrastructure.character

import com.fasterxml.jackson.annotation.JsonProperty

enum class MediaCharacterRole {
    @JsonProperty("main")
    MAIN,
    @JsonProperty("supporting")
    SUPPORTING,
    @JsonProperty("recurring")
    RECURRING,
    @JsonProperty("cameo")
    CAMEO
}
