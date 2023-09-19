package io.github.drumber.kitsune.domain.manager

sealed class LibraryUpdateResponse {
    /** The library entry could be updated online and is in sync with the upstream server. */
    data object SyncedOnline : LibraryUpdateResponse()

    /** The library entry couldn't be updated online and the offline library update is cached in the local database. */
    data object OfflineCache : LibraryUpdateResponse()

    /** Failed to update library entry. */
    data class Error(val exception: Exception) : LibraryUpdateResponse()
}
