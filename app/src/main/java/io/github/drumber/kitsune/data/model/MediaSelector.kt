package io.github.drumber.kitsune.data.model

import android.os.Parcelable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaSelector(
    val mediaType: MediaType,
    val filter: Filter,
    val requestType: RequestType = RequestType.ALL
): Parcelable

enum class MediaType(val type: String) {
    Anime("anime"), Manga("manga")
}

fun MediaType.toStringRes() = when (this) {
    MediaType.Anime -> R.string.anime
    MediaType.Manga -> R.string.manga
}
