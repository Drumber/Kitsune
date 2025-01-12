package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.library.LibraryEntryMediaType
import io.github.drumber.kitsune.data.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.extension.getFilterValue

data class LibraryEntryFilter(
    val kind: LibraryEntryMediaType,
    val libraryStatus: List<LibraryStatus>,
    private val initialFilter: Filter = Filter()
) {

    fun pageSize(pageSize: Int): LibraryEntryFilter {
        return copy(initialFilter = Filter(initialFilter.options.toMutableMap()).pageLimit(pageSize))
    }

    fun buildFilter() = Filter(initialFilter.options.toMutableMap()).apply {
            if (kind != LibraryEntryMediaType.All) {
                filter("kind", kind.name.lowercase())
            }
            if (libraryStatus.isNotEmpty()) {
                val status = libraryStatus.joinToString(",") { it.getFilterValue() }
                filter("status", status)
            }
        }

    fun isFiltered() = kind != LibraryEntryMediaType.All || libraryStatus.isNotEmpty()

    /** Checks if the initial filter has a 'title' filter applied. */
    fun isFilteredBySearchQuery() = initialFilter.hasFilterAttribute("title")

}

fun LibraryEntryFilter.toLibraryFilterOptions() = LibraryFilterOptions(
    mediaType = kind,
    status = libraryStatus
)
