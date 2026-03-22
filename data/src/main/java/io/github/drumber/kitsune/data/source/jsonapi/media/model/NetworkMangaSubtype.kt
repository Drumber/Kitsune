package io.github.drumber.kitsune.data.source.jsonapi.media.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class NetworkMangaSubtype {
    @JsonProperty("doujin")
    Doujin,
    @JsonProperty("manga")
    Manga,
    @JsonProperty("manhua")
    Manhua,
    @JsonProperty("manhwa")
    Manhwa,
    @JsonProperty("novel")
    Novel,
    @JsonProperty("oel")
    Oel,
    @JsonProperty("oneshot")
    Oneshot
}