package io.github.drumber.kitsune.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class SearchPagingDataSource<T : Any>(
    private val searcher: SearcherSingleIndex,
    private val transformer: (ResponseSearch.Hit) -> T
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            val queryLoaded = searcher.query.query

            val page = params.key ?: 0
            searcher.query.hitsPerPage = params.loadSize
            searcher.query.page = page
            searcher.isLoading.value = true

            val response = searcher.search()
            if (queryLoaded != searcher.query.query) {
                invalidate()
            }
            val nextKey = if (page + 1 < response.nbPages) page.plus(1) else null

            withContext(searcher.coroutineScope.coroutineContext) {
                searcher.response.value = response
                searcher.isLoading.value = false
            }

            LoadResult.Page(
                data = response.hits.map(transformer),
                prevKey = if (page > 0) page - 1 else null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            if (e !is CancellationException) {
                logE("Error receiving data from algolia search.", e)
            }
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>) = state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
    }

}