package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry

sealed class LibraryEntryUpdateResult {
    data class Success(val updatedLibraryEntry: LibraryEntry) : LibraryEntryUpdateResult()
    data class Failure(val reason: LibraryEntryUpdateFailureReason) : LibraryEntryUpdateResult()
}

sealed class LibraryEntryUpdateFailureReason {
    data object NotFound : LibraryEntryUpdateFailureReason()
    data class NetworkError(val exception: Exception) : LibraryEntryUpdateFailureReason()
    data class UnknownException(val exception: Exception) : LibraryEntryUpdateFailureReason()
}