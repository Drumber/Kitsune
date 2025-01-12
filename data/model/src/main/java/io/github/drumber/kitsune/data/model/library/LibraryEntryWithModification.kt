package io.github.drumber.kitsune.data.model.library

data class LibraryEntryWithModification(
    val libraryEntry: LibraryEntry,
    val modification: LibraryEntryModification?
) {

    val id
        get() = libraryEntry.id

    val media
        get() = libraryEntry.media

    val episodeCount: Int?
        get() = media?.episodeOrChapterCount

    val hasEpisodesCount: Boolean
        get() = episodeCount != null

    val episodeCountFormatted: String
        get() = episodeCount?.toString() ?: "∞"

    val progress
        get() = modification?.progress ?: libraryEntry.progress

    val hasStartedWatching: Boolean
        get() = progress?.equals(0) == false

    val hasStartedWatchingOrIsCurrent: Boolean
        get() = hasStartedWatching || status == LibraryStatus.Current

    val canWatchEpisode: Boolean
        get() = progress != episodeCount

    val volumesOwned
        get() = modification?.volumesOwned ?: libraryEntry.volumesOwned

    val ratingTwenty
        get() = modification?.ratingTwenty ?: libraryEntry.ratingTwenty

    val hasRating: Boolean
        get() = ratingTwenty != null && ratingTwenty != -1

    val status
        get() = modification?.status ?: libraryEntry.status

    val reconsumeCount
        get() = modification?.reconsumeCount ?: libraryEntry.reconsumeCount

    val isPrivate
        get() = modification?.privateEntry ?: libraryEntry.privateEntry

    val startedAt
        get() = modification?.startedAt ?: libraryEntry.startedAt

    val finishedAt
        get() = modification?.finishedAt ?: libraryEntry.finishedAt

    val notes
        get() = modification?.notes ?: libraryEntry.notes

    val isSynchronizing
        get() = modification?.state == LibraryModificationState.SYNCHRONIZING

    val isNotSynced
        get() = !isSynchronizing &&
                modification != null &&
                !modification.isEqualToLibraryEntry(libraryEntry)

}
