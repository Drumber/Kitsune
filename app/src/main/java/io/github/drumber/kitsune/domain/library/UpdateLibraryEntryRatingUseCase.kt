package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification

class UpdateLibraryEntryRatingUseCase(
    private val updateLibraryEntry: UpdateLibraryEntryUseCase
) {

    suspend operator fun invoke(
        libraryEntry: LibraryEntry,
        rating: Int?
    ): LibraryEntryUpdateResult {
        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer

        val modification = LibraryEntryModification.withIdAndNulls(libraryEntry.id)
            .copy(ratingTwenty = updatedRating)

        return updateLibraryEntry(modification)
    }
}