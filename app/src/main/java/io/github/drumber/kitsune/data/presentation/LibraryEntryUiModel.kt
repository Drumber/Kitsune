package io.github.drumber.kitsune.data.presentation

import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(
        val status: LibraryStatus,
        val isMangaSelected: Boolean
    ) : LibraryEntryUiModel()

    data class EntryModel(val entry: LibraryEntryWithModification) : LibraryEntryUiModel()
}
