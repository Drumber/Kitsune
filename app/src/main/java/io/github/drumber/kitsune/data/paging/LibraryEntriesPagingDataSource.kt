package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService

class LibraryEntriesPagingDataSource(
    private val service: LibraryEntriesService,
    filter: Filter
) : BasePagingDataSource<LibraryEntry>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<LibraryEntry>> {
        return service.allLibraryEntries(filter.options)
    }
}
