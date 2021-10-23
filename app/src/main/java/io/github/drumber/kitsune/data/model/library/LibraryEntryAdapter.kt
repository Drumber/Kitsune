package io.github.drumber.kitsune.data.model.library

class LibraryEntryAdapter(val libraryEntry: LibraryEntry) {

    val episodeCount: Int?
        get() = libraryEntry.anime?.episodeCount ?: libraryEntry.manga?.volumeCount

    val episodes: String
        get() = episodeCount?.toString() ?: "?"

    val progressCount: Int?
        get() = libraryEntry.progress

    val progress: String
        get() = progressCount?.toString() ?: "?"

    val hasEpisodesCount: Boolean
        get() = episodeCount != null

    val hasStartedWatching: Boolean
        get() = progressCount?.equals(0) == false

    val canWatchEpisode: Boolean
        get() = progressCount != episodeCount

    val canUnwatchEpisode: Boolean
        get() = hasStartedWatching

}