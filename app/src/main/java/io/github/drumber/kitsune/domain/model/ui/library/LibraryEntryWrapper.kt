package io.github.drumber.kitsune.domain.model.ui.library

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.library.LibraryModification
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibraryEntryWrapper(
    val libraryEntry: LibraryEntry,
    val libraryModification: LibraryModification?
) : LibraryEntryUiModel(), Parcelable {

    val progress
        get() = libraryModification?.progress ?: libraryEntry.progress

    val volumesOwned
        get() = libraryModification?.volumesOwned ?: libraryEntry.volumesOwned

    val ratingTwenty
        get() = libraryModification?.ratingTwenty ?: libraryEntry.ratingTwenty

    val status
        get() = libraryModification?.status ?: libraryEntry.status

    val reconsumeCount
        get() = libraryModification?.reconsumeCount ?: libraryEntry.reconsumeCount

    val isPrivate
        get() = libraryModification?.isPrivate ?: libraryEntry.privateEntry

    val startedAt
        get() = libraryModification?.startedAt ?: libraryEntry.startedAt

    val finishedAt
        get() = libraryModification?.finishedAt ?: libraryEntry.finishedAt

    val notes
        get() = libraryModification?.notes ?: libraryEntry.notes

}
