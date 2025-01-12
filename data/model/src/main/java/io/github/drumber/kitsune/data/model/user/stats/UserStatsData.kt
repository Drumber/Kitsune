package io.github.drumber.kitsune.data.model.user.stats

sealed class UserStatsData {
    data class CategoryBreakdownData(
        val total: Int?,
        val categories: Map<String, Int>?
    ) : UserStatsData()

    data class AmountConsumedData(
        val time: Long?,
        val media: Int?,
        val units: Int?,
        val completed: Int?,
        val percentiles: AmountConsumedPercentiles?,
        val averageDiffs: AmountConsumedPercentiles?
    ) : UserStatsData()
}