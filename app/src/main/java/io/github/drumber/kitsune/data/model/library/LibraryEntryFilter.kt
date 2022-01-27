package io.github.drumber.kitsune.data.model.library

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

    // Note: Do not apply filters to the network-request filter object to allow offline caching
    // of the whole user library instead of the last filtered library response.
    // Filtering will be performed only locally on the Room database.
    // Maybe add a preference to disable caching of the whole library to reduce cache size.
    fun buildFilter() = Filter(initialFilter.options.toMutableMap())/*.apply {
            if (kind != LibraryEntryKind.All) {
                filter("kind", kind.name.lowercase())
            }
            if (libraryStatus.isNotEmpty()) {
                val objectMapper = jacksonObjectMapper()
                val status = libraryStatus.joinToString(",") { objectMapper.writeValueAsString(it) }
                filter("status", status)
            }
        }*/

}

enum class LibraryEntryKind {
    All,
    Anime,
    Manga
}
