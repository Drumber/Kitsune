package io.github.drumber.kitsune.data.source.graphql.library

import com.apollographql.apollo.ApolloClient
import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryApolloDataSource(
    private val client: ApolloClient
) {

    suspend fun getLibraryEntries(): GetLibraryEntriesQuery.Data {
        return withContext(Dispatchers.IO) {
            client.query(GetLibraryEntriesQuery()).execute().dataAssertNoErrors
        }
    }
}