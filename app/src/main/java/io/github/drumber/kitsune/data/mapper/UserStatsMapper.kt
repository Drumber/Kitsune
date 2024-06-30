package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.user.stats.AmountConsumedPercentiles
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData.AmountConsumedData
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData.CategoryBreakdownData
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsKind
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkAmountConsumedPercentiles
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStats
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStatsData
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStatsData.NetworkAmountConsumedData
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStatsData.NetworkCategoryBreakdownData
import io.github.drumber.kitsune.data.source.network.user.model.stats.NetworkUserStatsKind

object UserStatsMapper {
    fun NetworkUserStats.toUserStats() = UserStats(
        id = id.require(),
        kind = kind?.toUserStatsKind(),
        statsData = statsData?.toUserStatsData()
    )

    private fun NetworkUserStatsKind.toUserStatsKind() = when (this) {
        NetworkUserStatsKind.AnimeActivityHistory -> UserStatsKind.AnimeActivityHistory
        NetworkUserStatsKind.AnimeAmountConsumed -> UserStatsKind.AnimeAmountConsumed
        NetworkUserStatsKind.AnimeCategoryBreakdown -> UserStatsKind.AnimeCategoryBreakdown
        NetworkUserStatsKind.AnimeFavoriteYear -> UserStatsKind.AnimeFavoriteYear
        NetworkUserStatsKind.MangaActivityHistory -> UserStatsKind.MangaActivityHistory
        NetworkUserStatsKind.MangaAmountConsumed -> UserStatsKind.MangaAmountConsumed
        NetworkUserStatsKind.MangaCategoryBreakdown -> UserStatsKind.MangaCategoryBreakdown
        NetworkUserStatsKind.MangaFavoriteYear -> UserStatsKind.MangaFavoriteYear
    }

    private fun NetworkUserStatsData.toUserStatsData() = when (this) {
        is NetworkAmountConsumedData -> this.toAmountConsumedData()
        is NetworkCategoryBreakdownData -> this.toCategoryBreakdownData()
    }

    private fun NetworkAmountConsumedData.toAmountConsumedData() = AmountConsumedData(
        time = time,
        media = media,
        units = units,
        completed = completed,
        percentiles = percentiles?.toAmountConsumedPercentiles(),
        averageDiffs = averageDiffs?.toAmountConsumedPercentiles()
    )

    private fun NetworkCategoryBreakdownData.toCategoryBreakdownData() = CategoryBreakdownData(
        total = total,
        categories = categories
    )

    private fun NetworkAmountConsumedPercentiles.toAmountConsumedPercentiles() = AmountConsumedPercentiles(
        media = media,
        units = units,
        time = time
    )
}