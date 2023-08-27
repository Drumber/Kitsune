package io.github.drumber.kitsune.domain.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService

class LibraryEntriesPagingDataSource(
    private val service: LibraryEntriesService,
    filter: Filter
) : BasePagingDataSource<LibraryEntry>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<LibraryEntry>> {
        return service.allLibraryEntries(filter.options)
    }
}
