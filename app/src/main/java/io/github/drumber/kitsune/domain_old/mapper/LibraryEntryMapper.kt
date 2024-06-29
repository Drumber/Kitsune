package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain_old.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.exception.InvalidDataException

fun LibraryEntry.toLocalLibraryEntry() = LocalLibraryEntry(
    id = id ?: throw InvalidDataException("ID cannot be 'null'."),
    updatedAt = updatedAt,
    startedAt = startedAt,
    finishedAt = finishedAt,
    progressedAt = progressedAt,
    status = status,
    progress = progress,
    reconsuming = reconsuming,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = ratingTwenty,
    notes = notes,
    privateEntry = privateEntry,
    reactionSkipped = reactionSkipped,
    anime = anime?.toLocalAnime(),
    manga = manga?.toLocalManga()
)

fun LocalLibraryEntry.toLibraryEntry() = LibraryEntry(
    id = id,
    updatedAt = updatedAt,
    startedAt = startedAt,
    finishedAt = finishedAt,
    progressedAt = progressedAt,
    status = status,
    progress = progress,
    reconsuming = reconsuming,
    reconsumeCount = reconsumeCount,
    volumesOwned = volumesOwned,
    ratingTwenty = ratingTwenty,
    notes = notes,
    privateEntry = privateEntry,
    reactionSkipped = reactionSkipped,
    anime = anime?.toAnime(),
    manga = manga?.toManga(),
    user = null
)
