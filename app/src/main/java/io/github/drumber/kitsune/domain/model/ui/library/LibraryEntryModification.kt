package io.github.drumber.kitsune.domain.model.ui.library

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class LibraryEntryModification(
    /** Corresponds to the library entry ID */
    val id: String,

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
) : Parcelable
