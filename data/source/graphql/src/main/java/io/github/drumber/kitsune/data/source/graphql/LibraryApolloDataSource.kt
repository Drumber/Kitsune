package io.github.drumber.kitsune.data.source.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.model.library.LibraryStatus
import io.github.drumber.kitsune.data.common.mapper.toMediaType
import io.github.drumber.kitsune.data.source.graphql.fragment.LibraryEntryWithNextUnitFragment
import io.github.drumber.kitsune.data.source.graphql.mapper.toLibraryEntrySortEnum
import io.github.drumber.kitsune.data.source.graphql.mapper.toLibraryEntryStatusEnum
import io.github.drumber.kitsune.data.source.graphql.mapper.toMediaTypeEnum
import io.github.drumber.kitsune.data.source.graphql.mapper.toSortDirection
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
                    mediaType = Optional.presentIfNotNull(filter.mediaType.toMediaType()?.toMediaTypeEnum()),
                    sort = Optional.presentIfNotNull(filter.sortBy?.toLibraryEntrySortEnum()),
                    sortDirection = Optional.presentIfNotNull(filter.sortDirection?.toSortDirection())
                )
            ).execute().data?.currentProfile?.library?.all
        }
    }

    suspend fun updateProgress(
        libraryEntryId: String,
        progress: Int
    ): LibraryEntryWithNextUnitFragment? {
        return withContext(Dispatchers.IO) {
            val mutation = UpdateLibraryEntryProgressMutation(libraryEntryId, progress)
            client.mutation(mutation)
                .execute().data?.libraryEntry?.updateProgressById?.libraryEntry?.libraryEntryWithNextUnitFragment
        }
    }
}