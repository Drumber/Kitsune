package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.common.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.Media

data class LibraryEntry(
    val id: String,
    val updatedAt: String?,

    val startedAt: String?,
    val finishedAt: String?,
    val progressedAt: String?,

    val status: LibraryStatus?,
    val progress: Int?,
    val reconsuming: Boolean?,
    val reconsumeCount: Int?,
    val volumesOwned: Int?,
    val ratingTwenty: Int?,

    val notes: String?,
    val privateEntry: Boolean?,
    val reactionSkipped: ReactionSkip?,

    val media: Media?
)
