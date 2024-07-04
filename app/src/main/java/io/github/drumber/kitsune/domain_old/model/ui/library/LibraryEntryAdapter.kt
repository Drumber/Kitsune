package io.github.drumber.kitsune.domain_old.model.ui.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.formatRatingTwenty

class LibraryEntryAdapter(private val wrapper: LibraryEntryWrapper) {

    private val libraryEntry get() = wrapper.libraryEntry

    val id: String
        get() = libraryEntry.id!!

    val episodeCount: Int?
        get() = libraryEntry.anime?.episodeCount ?: libraryEntry.manga?.chapterCount

    val episodes: String
        get() = episodeCount?.toString() ?: "∞"

    val progressCount: Int?
        get() = wrapper.progress

    val progress: String
        get() = progressCount?.toString() ?: "-"

    val hasEpisodesCount: Boolean
        get() = episodeCount != null

    val hasStartedWatching: Boolean
        get() = progressCount?.equals(0) == false

    val hasStartedWatchingOrIsCurrent: Boolean
        get() = hasStartedWatching || wrapper.status == LibraryStatus.Current

    val canWatchEpisode: Boolean
        get() = progressCount != episodeCount

    val rating: String?
        get() = wrapper.ratingTwenty?.formatRatingTwenty()

    val hasRating: Boolean
        get() = wrapper.ratingTwenty != null

}