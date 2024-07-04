package io.github.drumber.kitsune.domain.library

import androidx.paging.PagingData
import androidx.paging.map
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetLibraryEntriesWithModificationsPagerUseCase(
    private val libraryRepository: LibraryRepository
) {

    operator fun invoke(
        pageSize: Int,
        filter: LibraryEntryFilter
    ): Flow<PagingData<LibraryEntryWithModification>> {
        return libraryRepository.libraryEntriesPager(pageSize, filter)
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