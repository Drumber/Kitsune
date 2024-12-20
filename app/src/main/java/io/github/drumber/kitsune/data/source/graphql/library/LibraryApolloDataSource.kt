package io.github.drumber.kitsune.data.source.graphql.library

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.mapper.graphql.toLibraryEntrySortEnum
import io.github.drumber.kitsune.data.mapper.graphql.toLibraryEntryStatusEnum
import io.github.drumber.kitsune.data.mapper.graphql.toMediaTypeEnum
import io.github.drumber.kitsune.data.mapper.graphql.toSortDirection
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesWithNextUnitQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryApolloDataSource(
    private val client: ApolloClient
) {

    suspend fun getLibraryEntriesWithNextUnit(
        cursor: String? = null,
        pageSize: Int,
        filter: LibraryFilterOptions
    ): GetLibraryEntriesWithNextUnitQuery.All? {
        return withContext(Dispatchers.IO) {
            client.query(
                GetLibraryEntriesWithNextUnitQuery(
                    cursor = Optional.presentIfNotNull(cursor),
                    pageSize = Optional.present(pageSize),
                    status = Optional.presentIfNotNull(filter.status?.map(LibraryStatus::toLibraryEntryStatusEnum)),
                    mediaType = Optional.presentIfNotNull(filter.mediaType?.toMediaTypeEnum()),
                    sort = Optional.presentIfNotNull(filter.sortBy?.toLibraryEntrySortEnum()),
                    sortDirection = Optional.presentIfNotNull(filter.sortDirection?.toSortDirection())
                )
            ).execute().data?.currentProfile?.library?.all
        }
    }
}