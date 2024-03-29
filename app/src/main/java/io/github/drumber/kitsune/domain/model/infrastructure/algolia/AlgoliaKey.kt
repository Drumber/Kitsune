package io.github.drumber.kitsune.domain.model.infrastructure.algolia

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlgoliaKey(
    val key: String? = null,
    val index: String? = null
) : Parcelable
