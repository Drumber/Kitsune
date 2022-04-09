package io.github.drumber.kitsune.data.model.library

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.drumber.kitsune.data.service.Filter

data class LibraryEntryFilter(
    val kind: LibraryEntryKind,
    val libraryStatus: List<Status>,
    private val initialFilter: Filter = Filter()
) {

    fun pageSize(pageSize: Int): LibraryEntryFilter {
        initialFilter.pageLimit(pageSize)
        return this
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

}

enum class LibraryEntryKind {
    All,
    Anime,
    Manga
}
