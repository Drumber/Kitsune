package io.github.drumber.kitsune.domain_old.model.ui.library

import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(val status: LibraryStatus, val isMangaSelected: Boolean): LibraryEntryUiModel()
    /** Child: [LibraryEntryWrapper] */
}
