package io.github.drumber.kitsune.data.model

import android.os.Parcelable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceSelector(
    val resourceType: ResourceType,
    val filter: Filter,
    val requestType: RequestType = RequestType.ALL
): Parcelable

enum class ResourceType(val type: String) {
    Anime("anime"), Manga("manga")
}

fun ResourceType.toStringRes() = when (this) {
    ResourceType.Anime -> R.string.anime
    ResourceType.Manga -> R.string.manga
}
