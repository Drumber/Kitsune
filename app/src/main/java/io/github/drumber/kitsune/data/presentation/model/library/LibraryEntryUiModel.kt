package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.common.library.LibraryStatus

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(
        val status: LibraryStatus,
        val isMangaSelected: Boolean
    ) : LibraryEntryUiModel()

    data class EntryModel(val entry: LibraryEntryWithModification) : LibraryEntryUiModel()
}
