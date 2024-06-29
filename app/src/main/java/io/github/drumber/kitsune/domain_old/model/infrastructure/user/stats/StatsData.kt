package io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonSubTypes
import kotlinx.parcelize.Parcelize

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
sealed class StatsData : Parcelable {
    @Parcelize
    data class CategoryBreakdownData(
        val total: Int?,
        val categories: Map<String, Int>?
    ) : StatsData()

    @Parcelize
    data class AmountConsumedData(
        val time: Long?,
        val media: Int?,
        val units: Int?,
        val completed: Int?,
        val percentiles: AmountConsumedPercentiles?,
        val averageDiffs: AmountConsumedPercentiles?
    ) : StatsData()
}