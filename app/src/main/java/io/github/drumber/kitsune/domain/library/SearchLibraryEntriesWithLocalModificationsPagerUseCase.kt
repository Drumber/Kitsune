package io.github.drumber.kitsune.domain.library

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.repository.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SearchLibraryEntriesWithLocalModificationsPagerUseCase(
    private val libraryRepository: LibraryRepository
) {

    operator fun invoke(
        pageSize: Int,
        filter: Filter,
        cacheScope: CoroutineScope
    ): Flow<PagingData<LibraryEntryWithModification>> {
        return libraryRepository.searchLibraryEntriesPager(pageSize, filter)
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