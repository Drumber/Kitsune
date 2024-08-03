package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.util.formatUtcDate
import io.github.drumber.kitsune.util.getLocalCalendar

class UpdateLibraryEntryProgressUseCase(
    private val updateLibraryEntry: UpdateLibraryEntryUseCase
) {

    suspend operator fun invoke(
        libraryEntry: LibraryEntry,
        newProgress: Int
    ): LibraryEntryUpdateResult {
        var modification = LibraryEntryModification.withIdAndNulls(libraryEntry.id)
            .copy(progress = newProgress)

        // set startedAt date when starting consuming library entry
        if (
            libraryEntry.startedAt.isNullOrBlank() &&
            newProgress == 1 &&
            (libraryEntry.progress ?: 0) == 0
        ) {
            modification = modification.copy(
                startedAt = getLocalCalendar().formatUtcDate()
            )
        }

        return updateLibraryEntry(modification)
    }
}