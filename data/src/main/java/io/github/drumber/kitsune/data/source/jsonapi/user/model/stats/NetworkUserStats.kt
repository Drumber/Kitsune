package io.github.drumber.kitsune.data.source.jsonapi.user.model.stats

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("stats")
data class NetworkUserStats(
    @Id
    val id: String?,
    val kind: NetworkUserStatsKind?,

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "kind",
        visible = true
    )
    val statsData: NetworkUserStatsData?
)
