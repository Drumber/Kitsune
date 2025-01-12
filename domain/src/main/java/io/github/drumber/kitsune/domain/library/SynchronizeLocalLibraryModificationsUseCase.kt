package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.repository.library.LibraryRepository

class SynchronizeLocalLibraryModificationsUseCase(
    private val libraryRepository: LibraryRepository,
    private val updateLibraryEntry: UpdateLibraryEntryUseCase
) {

    suspend operator fun invoke(): Map<String, LibraryEntryUpdateResult> {
        return libraryRepository.getAllLibraryEntryModifications()
            .associate { libraryEntryModification ->
                libraryEntryModification.id to updateLibraryEntry(
                    libraryEntryModification
                )
            }
    }
}