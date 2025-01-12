package io.github.drumber.kitsune.domain.library

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.repository.library.LibraryPagingRepository
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SearchLibraryEntriesWithLocalModificationsPagerUseCase(
    private val libraryRepository: LibraryRepository,
    private val libraryPagingRepository: LibraryPagingRepository
) {

    operator fun invoke(
        pageSize: Int,
        filter: Filter,
        cacheScope: CoroutineScope
    ): Flow<PagingData<LibraryEntryWithModification>> {
        return libraryPagingRepository.searchLibraryEntriesPager(pageSize, filter)
            .cachedIn(cacheScope)
            .combine(libraryRepository.getLibraryEntryModificationsAsFlow()) { pagingData, modifications ->
                pagingData.map { entry ->
                    LibraryEntryWithModification(
                        entry,
                        modifications.find { it.id == entry.id }
                    )
                }
            }
    }
}