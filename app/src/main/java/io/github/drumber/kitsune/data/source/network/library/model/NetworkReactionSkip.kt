package io.github.drumber.kitsune.data.source.network.library.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkReactionSkip {
    @JsonProperty("unskipped")
    Unskipped,

    @JsonProperty("skipped")
    Skipped,

    @JsonProperty("ignored")
    Ignored
}
