package io.github.drumber.kitsune.data.presentation.model.media

import io.github.drumber.kitsune.domain_old.service.Filter

data class MediaSelector(
    val mediaType: MediaType,
    val filter: Filter,
    val requestType: RequestType = RequestType.ALL
)

enum class MediaType(val type: String) {
    Anime("anime"), Manga("manga")
}

enum class RequestType {
    ALL,
    TRENDING
}
