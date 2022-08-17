package io.github.drumber.kitsune.data.model.library

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibraryEntryWrapper(
    val libraryEntry: LibraryEntry,
    val libraryModification: LibraryModification?
) : Parcelable {

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
        get() = libraryModification?.isPrivate ?: libraryEntry.isPrivate

    val startedAt
        get() = libraryModification?.startedAt ?: libraryEntry.startedAt

    val finishedAt
        get() = libraryModification?.finishedAt ?: libraryEntry.finishedAt

    val notes
        get() = libraryModification?.notes ?: libraryEntry.notes

}
