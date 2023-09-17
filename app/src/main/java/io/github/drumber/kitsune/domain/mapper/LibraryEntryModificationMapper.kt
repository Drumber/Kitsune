package io.github.drumber.kitsune.domain.mapper

import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryModification

fun LibraryEntryModification.toLocalLibraryEntryModification() = LocalLibraryEntryModification(
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
