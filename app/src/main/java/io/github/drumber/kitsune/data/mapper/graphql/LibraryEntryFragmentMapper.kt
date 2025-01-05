package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.source.graphql.fragment.LibraryEntryFragment
import io.github.drumber.kitsune.data.source.graphql.mapper.toLibraryStatus

fun LibraryEntryFragment.toLibraryEntry() = LibraryEntry(
    id = id,
    updatedAt = updatedAt,
    startedAt = startedAt,
    finishedAt = finishedAt,
    progressedAt = progressedAt,
    status = status.toLibraryStatus(),
    progress = progress,
    reconsuming = reconsuming,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = rating,
    notes = notes,
    privateEntry = private,
    reactionSkipped = null,
    media = media.libraryMediaFragment.toMedia()
)
