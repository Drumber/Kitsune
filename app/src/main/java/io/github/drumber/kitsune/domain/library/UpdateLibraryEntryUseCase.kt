package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NetworkError
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NotFound
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.UnknownException
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Failure
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Success
import io.github.drumber.kitsune.data.common.exception.NotFoundException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class UpdateLibraryEntryUseCase(
    private val libraryRepository: LibraryRepository
) {

    suspend operator fun invoke(modification: LibraryEntryModification): LibraryEntryUpdateResult {
        return try {
            val updatedLibraryEntry = libraryRepository.updateLibraryEntry(modification)
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