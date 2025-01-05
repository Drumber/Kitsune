package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.exception.NotFoundException
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NetworkError
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NotFound
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.UnknownException
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Failure
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Success
import io.github.drumber.kitsune.shared.formatUtcDate
import io.github.drumber.kitsune.shared.getLocalCalendar
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class UpdateLibraryEntryProgressUseCase(
    private val updateLibraryEntry: UpdateLibraryEntryUseCase,
    private val libraryRepository: LibraryRepository
) {

    suspend operator fun invoke(
        libraryEntry: LibraryEntry,
        newProgress: Int
    ): LibraryEntryUpdateResult {
        // set startedAt date when starting consuming library entry
        if (
            libraryEntry.startedAt.isNullOrBlank() &&
            newProgress == 1 &&
            (libraryEntry.progress ?: 0) == 0
        ) {
            val modification = LibraryEntryModification.withIdAndNulls(libraryEntry.id).copy(
                progress = newProgress,
                startedAt = getLocalCalendar().formatUtcDate()
            )

            return updateLibraryEntry(modification)
        }

        return try {
            val updatedLibraryEntry = libraryRepository.updateLibraryEntryProgress(
                libraryEntryId = libraryEntry.id,
                progress = newProgress
            )
            Success(updatedLibraryEntry)
        } catch (e: CancellationException) {
            throw e
        } catch (e: NotFoundException) {
            Failure(NotFound)
        } catch (e: IOException) {
            Failure(NetworkError(e))
        } catch (e: Exception) {
            Failure(UnknownException(e))
        }
    }
}