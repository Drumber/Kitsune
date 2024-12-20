package io.github.drumber.kitsune.data.common.library

import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus

data class LibraryFilterOptions(
    val status: List<LibraryStatus>? = null,
    val mediaType: MediaType? = null,
    val sortBy: SortBy? = null,
    val sortDirection: SortDirection? = null
) {
    enum class SortBy {
        STATUS,
        STARTED_AT,
        UPDATED_AT,
        PROGRESS,
        RATING
    }

    enum class SortDirection {
        ASC,
        DESC
    }
}
