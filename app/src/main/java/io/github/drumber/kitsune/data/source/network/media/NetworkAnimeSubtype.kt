package io.github.drumber.kitsune.data.source.network.media

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkAnimeSubtype {
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