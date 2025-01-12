package io.github.drumber.kitsune.data.model.library

data class LibraryFilterOptions(
    val status: List<LibraryStatus>? = null,
    val mediaType: LibraryEntryMediaType = LibraryEntryMediaType.All,
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
