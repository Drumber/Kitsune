package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry

sealed interface SynchronizationResult {
    data class Success(val libraryEntry: LibraryEntry) : SynchronizationResult
    data class Failed(val exception: Exception) : SynchronizationResult
    data object NotFound : SynchronizationResult
}
