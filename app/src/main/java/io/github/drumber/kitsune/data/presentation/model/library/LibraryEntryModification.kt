package io.github.drumber.kitsune.data.presentation.model.library

data class LibraryEntryModification(
    val id: String,

    val createTime: Long = System.currentTimeMillis(),
    val state: LibraryModificationState = LibraryModificationState.NOT_SYNCHRONIZED,

    val startedAt: String?,
    val finishedAt: String?,

    val status: LibraryStatus?,
    val progress: Int?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    /**  Set to `-1` to remove rating (will be mapped to `null` by the json serializer) */
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?
) {

    companion object {
        fun withIdAndNulls(id: String) = LibraryEntryModification(
            id = id,
            startedAt = null,
            finishedAt = null,
            status = null,
            progress = null,
            reconsumeCount = null,
            volumesOwned = null,
            ratingTwenty = null,
            notes = null,
            privateEntry = null
        )
    }

    /**
     * Apply modifications to the specified library entry.
     *
     * @param ignoreBlankNotes
     * Do not apply notes if modified notes is blank and library entry notes is null.
     *
     * @return the given library entry for convenience.
     */
    fun applyToLibraryEntry(
        libraryEntry: LibraryEntry, ignoreBlankNotes: Boolean = true
    ): LibraryEntry {
        return libraryEntry.copy(
            startedAt = startedAt ?: libraryEntry.startedAt,
            finishedAt = finishedAt ?: libraryEntry.finishedAt,
            status = status ?: libraryEntry.status,
            progress = progress ?: libraryEntry.progress,
            reconsumeCount = reconsumeCount ?: libraryEntry.reconsumeCount,
            volumesOwned = volumesOwned ?: libraryEntry.volumesOwned,
            ratingTwenty = ratingTwenty ?: libraryEntry.ratingTwenty,
            notes = notes
                ?.takeIf { !ignoreBlankNotes || libraryEntry.notes != null || it.isNotBlank() }
                ?: libraryEntry.notes,
            privateEntry = privateEntry ?: libraryEntry.privateEntry
        )
    }

    fun mergeModificationFrom(other: LibraryEntryModification) = LibraryEntryModification(
        id = id,
        status = other.status ?: status,
        progress = other.progress ?: progress,
        volumesOwned = other.volumesOwned ?: volumesOwned,
        reconsumeCount = other.reconsumeCount ?: reconsumeCount,
        notes = other.notes ?: notes,
        privateEntry = other.privateEntry ?: privateEntry,
        startedAt = other.startedAt ?: startedAt,
        finishedAt = other.finishedAt ?: finishedAt,
        ratingTwenty = other.ratingTwenty ?: ratingTwenty
    )

    fun toLocalLibraryEntry(ignoreBlankNotes: Boolean = true): LibraryEntry {
        return LibraryEntry(
            id = id,
            updatedAt = null,
            startedAt = startedAt,
            finishedAt = finishedAt,
            progressedAt = null,
            status = status,
            progress = progress,
            reconsuming = null,
            reconsumeCount = reconsumeCount,
            volumesOwned = volumesOwned,
            ratingTwenty = ratingTwenty,
            notes = notes?.takeIf { !ignoreBlankNotes || it.isNotBlank() },
            privateEntry = privateEntry,
            reactionSkipped = null,
            media = null
        )
    }

    fun isEqualToLibraryEntry(libraryEntry: LibraryEntry): Boolean {
        return libraryEntry == applyToLibraryEntry(libraryEntry)
    }
}
