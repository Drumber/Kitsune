package io.github.drumber.kitsune.domain.model.infrastructure.user.stats

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AmountConsumedPercentiles(
    val media: Float?,
    val units: Float?,
    val time: Float?
) : Parcelable
