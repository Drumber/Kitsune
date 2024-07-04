package io.github.drumber.kitsune.domain_old.model.ui.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(
        val status: LibraryStatus,
        val isMangaSelected: Boolean
    ) : LibraryEntryUiModel()

    data class EntryModel(val entry: LibraryEntryWithModification) : LibraryEntryUiModel()
}
