package io.github.drumber.kitsune.domain.model.infrastructure.media

import com.fasterxml.jackson.annotation.JsonProperty

enum class AnimeSubtype {
    @JsonProperty("ONA")
    ONA,
    @JsonProperty("OVA")
    OVA,
    @JsonProperty("TV")
    TV,
    @JsonProperty("movie")
    Movie,
    @JsonProperty("music")
    Music,
    @JsonProperty("special")
    Special
}