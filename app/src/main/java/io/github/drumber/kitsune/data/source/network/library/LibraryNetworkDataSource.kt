package io.github.drumber.kitsune.data.source.network.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.library.api.LibraryEntryApi
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.data.common.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryNetworkDataSource(
    private val libraryEntryApi: LibraryEntryApi
) {

    suspend fun getAllLibraryEntries(filter: Filter): PageData<NetworkLibraryEntry> {
        return withContext(Dispatchers.IO) {
            libraryEntryApi.getAllLibraryEntries(filter.options).toPageData()
        }
    }

    suspend fun getLibraryEntry(id: String, filter: Filter): NetworkLibraryEntry? {
        return withContext(Dispatchers.IO) {
            libraryEntryApi.getLibraryEntry(id, filter.options).get()
        }
    }

    suspend fun updateLibraryEntry(
        id: String,
        libraryEntry: NetworkLibraryEntry,
        filter: Filter = Filter()
    ): NetworkLibraryEntry? {
        return withContext(Dispatchers.IO) {
            libraryEntryApi.updateLibraryEntry(
                id,
                JSONAPIDocument(libraryEntry),
                filter.options
            ).get()
        }
    }

    suspend fun postLibraryEntry(
        libraryEntry: NetworkLibraryEntry,
        filter: Filter = Filter()
    ): NetworkLibraryEntry? {
        return withContext(Dispatchers.IO) {
            libraryEntryApi.postLibraryEntry(JSONAPIDocument(libraryEntry), filter.options).get()
        }
    }

    suspend fun deleteLibraryEntry(id: String) {
        return withContext(Dispatchers.IO) {
            libraryEntryApi.deleteLibraryEntry(id)
        }
    }
}