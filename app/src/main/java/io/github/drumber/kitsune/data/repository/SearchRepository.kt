package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.paging.SearchPagingDataSource
import java.util.concurrent.CopyOnWriteArrayList

object SearchRepository {

    private val pagingSources = CopyOnWriteArrayList<PagingSource<Int, *>>()

    fun <T : Any> search(
        pageSize: Int,
        searcher: SearcherSingleIndex,
        transformer: (ResponseSearch.Hit) -> T
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            SearchPagingDataSource(searcher, transformer).also { pagingSources.add(it) }
        }
    ).flow

    fun invalidate() {
        for (pagingSource in pagingSources) {
            if (!pagingSource.invalid) {
                pagingSource.invalidate()
            }
        }

        pagingSources.removeAll { it.invalid }
    }

}