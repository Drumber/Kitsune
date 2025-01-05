package io.github.drumber.kitsune.data.source.jsonapi.media.model.production

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkAnimeProductionRole {
    @JsonProperty("licensor")
    Licensor,
    @JsonProperty("producer")
    Producer,
    @JsonProperty("studio")
    Studio
}