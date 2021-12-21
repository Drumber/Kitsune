package io.github.drumber.kitsune.data.manager

sealed class LibraryUpdateResponse {
    object SyncedOnline : LibraryUpdateResponse()
    object OfflineCache : LibraryUpdateResponse()
    data class Error(val exception: Exception) : LibraryUpdateResponse()
}
