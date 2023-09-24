package io.github.drumber.kitsune.domain.model.common.media

import com.fasterxml.jackson.annotation.JsonProperty

enum class ReleaseStatus {
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