package io.github.drumber.kitsune.data.model

import android.os.Parcelable
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResourceSelector(
    val resourceType: ResourceType,
    val filter: Filter
): Parcelable

enum class ResourceType {
    Anime, Manga
}
