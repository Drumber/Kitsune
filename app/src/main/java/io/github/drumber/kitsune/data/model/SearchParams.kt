package io.github.drumber.kitsune.data.model

import android.os.Parcelable
import io.github.drumber.kitsune.constants.SortFilter
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchParams(
    val mediaType: MediaType,
    val categories: List<String>,
    val sortOrder: SortFilter
): Parcelable, Cloneable
