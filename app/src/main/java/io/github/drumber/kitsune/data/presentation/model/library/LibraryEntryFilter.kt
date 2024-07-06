package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind

data class LibraryEntryFilter(
    val kind: LibraryEntryKind,
    val libraryStatus: List<LibraryStatus>,
    private val initialFilter: Filter = Filter()
) {

    fun pageSize(pageSize: Int): LibraryEntryFilter {
        return copy(initialFilter = Filter(initialFilter.options.toMutableMap()).pageLimit(pageSize))
    }

    fun buildFilter() = Filter(initialFilter.options.toMutableMap()).apply {
            if (kind != LibraryEntryKind.All) {
                filter("kind", kind.name.lowercase())
            }
            if (libraryStatus.isNotEmpty()) {
                val status = libraryStatus.joinToString(",") { it.getFilterValue() }
                filter("status", status)
            }
        }

    fun isFiltered() = kind != LibraryEntryKind.All || libraryStatus.isNotEmpty()

    /** Checks if the initial filter has a 'title' filter applied. */
    fun isFilteredBySearchQuery() = initialFilter.hasFilterAttribute("title")

}
