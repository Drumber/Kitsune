package io.github.drumber.kitsune.data.presentation.model.media

import android.os.Parcelable
import io.github.drumber.kitsune.data.common.FilterOptions
import io.github.drumber.kitsune.data.common.media.MediaType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaSelector(
    val mediaType: MediaType,
    val filterOptions: FilterOptions,
    val requestType: RequestType = RequestType.ALL
) : Parcelable

val MediaType.identifier
    get() = when (this) {
        MediaType.Anime -> "anime"
        MediaType.Manga -> "manga"
    }

enum class RequestType {
    ALL,
    TRENDING
}
