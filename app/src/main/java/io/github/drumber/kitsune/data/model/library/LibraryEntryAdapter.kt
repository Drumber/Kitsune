package io.github.drumber.kitsune.data.model.library

class LibraryEntryAdapter(private val wrapper: LibraryEntryWrapper) {

    private val libraryEntry get() = wrapper.libraryEntry

    val episodeCount: Int?
        get() = libraryEntry.anime?.episodeCount ?: libraryEntry.manga?.chapterCount

    val episodes: String
        get() = episodeCount?.toString() ?: "?"

    val progressCount: Int?
        get() = wrapper.progress

    val progress: String
        get() = progressCount?.toString() ?: "?"

    val hasEpisodesCount: Boolean
        get() = episodeCount != null

    val hasStartedWatching: Boolean
        get() = progressCount?.equals(0) == false

    val canWatchEpisode: Boolean
        get() = progressCount != episodeCount

    val rating: String?
        get() = wrapper.ratingTwenty?.div(4.0f)?.toString()

    val hasRating: Boolean
        get() = libraryEntry.ratingTwenty != null

}