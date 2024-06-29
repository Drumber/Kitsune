package io.github.drumber.kitsune.domain_old.manager.library

import io.github.drumber.kitsune.domain_old.model.infrastructure.library.LibraryEntry

sealed interface SynchronizationResult {
    data class Success(val libraryEntry: LibraryEntry) : SynchronizationResult
    data class Failed(val exception: Exception) : SynchronizationResult
    data object NotFound : SynchronizationResult
}
