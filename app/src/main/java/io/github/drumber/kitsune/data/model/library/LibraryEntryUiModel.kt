package io.github.drumber.kitsune.data.model.library

sealed class LibraryEntryUiModel {
    data class StatusSeparatorModel(val status: Status): LibraryEntryUiModel()
    /** Child: [LibraryEntryWrapper] */
}
