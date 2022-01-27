package io.github.drumber.kitsune.data.model.stats

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("stats")
data class Stats(
    @Id val id: String?,
    val kind: StatsKind?,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "kind",
        visible = true
    )
    val statsData: StatsData?
) : Parcelable

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

@JsonSubTypes(
    JsonSubTypes.Type(
        value = StatsData.CategoryBreakdownData::class,
        names = ["anime-category-breakdown", "manga-category-breakdown"]
    ),
    JsonSubTypes.Type(
        value = StatsData.AmountConsumedData::class,
        names = ["anime-amount-consumed", "manga-amount-consumed"]
    )
)
sealed class StatsData: Parcelable {
    @Parcelize
    data class CategoryBreakdownData(
        val total: Int?,
        val categories: Map<String, Int>?
    ): StatsData()

    @Parcelize
    data class AmountConsumedData(
        val time: Long?,
        val media: Int?,
        val units: Int?,
        val completed: Int?,
        val percentiles: AmountConsumedPercentiles?,
        val averageDiffs: AmountConsumedPercentiles?
    ): StatsData()
}

@Parcelize
data class AmountConsumedPercentiles(
    val media: Float?,
    val units: Float?,
    val time: Float?
): Parcelable
