package io.github.drumber.kitsune.data.source.network.media.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkReleaseStatus {
    @JsonProperty("current")
    Current,
    @JsonProperty("finished")
    Finished,
    @JsonProperty("tba")
    TBA,
    @JsonProperty("unreleased")
    Unreleased,
    @JsonProperty("upcoming")
    Upcoming
}