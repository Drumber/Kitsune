package io.github.drumber.kitsune.domain.model.ui.library

import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(val status: LibraryStatus): LibraryEntryUiModel()
    /** Child: [LibraryEntryWrapper] */
}
