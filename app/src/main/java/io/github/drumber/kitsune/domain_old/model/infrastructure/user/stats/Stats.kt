package io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("stats")
data class Stats(
    @Id
    val id: String?,
    val kind: StatsKind?,

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "kind",
        visible = true
    )
    val statsData: StatsData?
) : Parcelable
