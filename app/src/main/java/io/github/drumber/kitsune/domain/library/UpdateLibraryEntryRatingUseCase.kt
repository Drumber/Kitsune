package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification

class UpdateLibraryEntryRatingUseCase(
    private val updateLibraryEntry: UpdateLibraryEntryUseCase
) {

    suspend operator fun invoke(
        libraryEntryId: String,
        rating: Int?
    ): LibraryEntryUpdateResult {
        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer

        val modification = LibraryEntryModification.withIdAndNulls(libraryEntryId)
            .copy(ratingTwenty = updatedRating)

        return updateLibraryEntry(modification)
    }
}