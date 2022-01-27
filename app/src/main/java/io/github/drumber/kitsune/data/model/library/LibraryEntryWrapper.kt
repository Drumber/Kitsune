package io.github.drumber.kitsune.data.model.library

data class LibraryEntryWrapper(
    val libraryEntry: LibraryEntry,
    val offlineLibraryUpdate: OfflineLibraryUpdate?
) {

    val progress
        get() = offlineLibraryUpdate?.progress ?: libraryEntry.progress

    val ratingTwenty
        get() = offlineLibraryUpdate?.ratingTwenty ?: libraryEntry.ratingTwenty

}
