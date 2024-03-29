package io.github.drumber.kitsune.domain.model.ui.library

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.service.Filter

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
                val objectMapper = jacksonObjectMapper()
                val status = libraryStatus.joinToString(",") { objectMapper.writeValueAsString(it) }
                filter("status", status)
            }
        }

    fun isFiltered() = kind != LibraryEntryKind.All || libraryStatus.isNotEmpty()

    /** Checks if the initial filter has a 'title' filter applied. */
    fun isFilteredBySearchQuery() = initialFilter.hasFilterAttribute("title")

}

enum class LibraryEntryKind {
    All,
    Anime,
    Manga
}
