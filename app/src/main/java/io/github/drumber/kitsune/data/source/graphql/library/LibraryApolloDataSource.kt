package io.github.drumber.kitsune.data.source.graphql.library

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesWithNextUnitQuery
import io.github.drumber.kitsune.data.source.graphql.type.LibraryEntryStatusEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryApolloDataSource(
    private val client: ApolloClient
) {

    suspend fun getLibraryEntriesWithNextUnit(
        pageSize: Int,
        status: List<LibraryEntryStatusEnum>
    ): GetLibraryEntriesWithNextUnitQuery.All? {
        return withContext(Dispatchers.IO) {
            client.query(
                GetLibraryEntriesWithNextUnitQuery(
                    pageSize = Optional.present(pageSize),
                    status = Optional.present(status)
                )
            ).execute().data?.currentProfile?.library?.all
        }
    }
}