package io.github.drumber.kitsune.data.model.user.stats

data class UserStats(
    val id: String,
    val kind: UserStatsKind?,
    val statsData: UserStatsData?
)
