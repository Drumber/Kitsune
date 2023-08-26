package io.github.drumber.kitsune.domain.model.infrastructure.library

import com.fasterxml.jackson.annotation.JsonProperty

enum class ReactionSkip {
    @JsonProperty("unskipped")
    Unskipped,

    @JsonProperty("skipped")
    Skipped,

    @JsonProperty("ignored")
    Ignored
}
