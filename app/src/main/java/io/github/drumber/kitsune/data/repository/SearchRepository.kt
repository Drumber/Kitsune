package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.paging.SearchPagingDataSource
import io.github.drumber.kitsune.data.service.Filter

object SearchRepository {

    fun <T : Resource> search(
        pageSize: Int,
        filter: Filter,
        searcher: SearcherSingleIndex,
        transformer: (ResponseSearch.Hit) -> T
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = { SearchPagingDataSource(searcher, filter, transformer) }
    ).flow

}