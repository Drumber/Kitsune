package io.github.drumber.kitsune.data.source.network.user.model.stats

import com.fasterxml.jackson.annotation.JsonSubTypes

@JsonSubTypes(
    JsonSubTypes.Type(
        value = NetworkUserStatsData.NetworkCategoryBreakdownData::class,
        names = ["anime-category-breakdown", "manga-category-breakdown"]
    ),
    JsonSubTypes.Type(
        value = NetworkUserStatsData.NetworkAmountConsumedData::class,
        names = ["anime-amount-consumed", "manga-amount-consumed"]
    )
)
sealed class NetworkUserStatsData {
    data class NetworkCategoryBreakdownData(
        val total: Int?,
        val categories: Map<String, Int>?
    ) : NetworkUserStatsData()

    data class NetworkAmountConsumedData(
        val time: Long?,
        val media: Int?,
        val units: Int?,
        val completed: Int?,
        val percentiles: NetworkAmountConsumedPercentiles?,
        val averageDiffs: NetworkAmountConsumedPercentiles?
    ) : NetworkUserStatsData()
}