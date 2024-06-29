package io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats

import com.fasterxml.jackson.annotation.JsonProperty

enum class StatsKind {
    @JsonProperty("anime-activity-history")
    AnimeActivityHistory,
    @JsonProperty("anime-amount-consumed")
    AnimeAmountConsumed,
    @JsonProperty("anime-category-breakdown")
    AnimeCategoryBreakdown,
    @JsonProperty("anime-favorite-year")
    AnimeFavoriteYear,

    @JsonProperty("manga-activity-history")
    MangaActivityHistory,
    @JsonProperty("manga-amount-consumed")
    MangaAmountConsumed,
    @JsonProperty("manga-category-breakdown")
    MangaCategoryBreakdown,
    @JsonProperty("manga-favorite-year")
    MangaFavoriteYear
}
