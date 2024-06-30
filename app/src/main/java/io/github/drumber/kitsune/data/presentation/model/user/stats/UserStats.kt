package io.github.drumber.kitsune.data.presentation.model.user.stats

data class UserStats(
    val id: String,
    val kind: UserStatsKind?,
    val statsData: UserStatsData?
)
