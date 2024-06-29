package io.github.drumber.kitsune.domain_old.model.common.media

import com.fasterxml.jackson.annotation.JsonProperty

enum class MangaSubtype {
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