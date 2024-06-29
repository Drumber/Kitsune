package io.github.drumber.kitsune.domain_old.model

import android.os.Parcelable
import io.github.drumber.kitsune.domain_old.paging.RequestType
import io.github.drumber.kitsune.domain_old.service.Filter
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
