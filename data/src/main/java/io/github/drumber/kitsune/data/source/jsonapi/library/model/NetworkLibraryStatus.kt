package io.github.drumber.kitsune.data.source.jsonapi.library.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkLibraryStatus {
    @JsonProperty("current")
    Current,

    @JsonProperty("planned")
    Planned,

    @JsonProperty("completed")
    Completed,

    @JsonProperty("on_hold")
    OnHold,

    @JsonProperty("dropped")
    Dropped
}