package io.github.drumber.kitsune.data.presentation.extension

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.formatRatingTwenty

val LibraryEntryWithModification.ratingFormatted: String?
    get() = ratingTwenty?.let {
        when {
            it == -1 -> null
            else -> it.formatRatingTwenty()
        }
    }