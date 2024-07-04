package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState.NOT_SYNCHRONIZED
import io.github.drumber.kitsune.domain_old.model.ui.library.LibraryEntryModification

fun LibraryEntryModification.toLocalLibraryEntryModification(
    state: LocalLibraryModificationState = NOT_SYNCHRONIZED
) = LocalLibraryEntryModification(
    id = id,
    state = state,
    startedAt = startedAt,
    finishedAt = finishedAt,
    status = status,
    progress = progress,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = ratingTwenty,
    notes = notes,
    privateEntry = privateEntry
)

fun LocalLibraryEntryModification.toLibraryEntryModification() = LibraryEntryModification(
    id = id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    status = status,
    progress = progress,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = ratingTwenty,
    notes = notes,
    privateEntry = privateEntry
)
