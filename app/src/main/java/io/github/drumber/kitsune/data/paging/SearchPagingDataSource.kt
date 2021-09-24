package io.github.drumber.kitsune.data.paging

import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.data.model.Page
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.withContext

class SearchPagingDataSource<T : Any>(
    private val searcher: SearcherSingleIndex,
    filter: Filter,
    private val transformer: (ResponseSearch.Hit) -> T
) : ResourcePagingDataSource<T>(filter, RequestType.ALL) {

    override suspend fun requestResource(
        filter: Filter,
        requestType: RequestType,
        params: LoadParams<Int>
    ): Response {
        val page = params.key ?: 0
        searcher.query.hitsPerPage = params.loadSize
        searcher.query.page = page
        searcher.isLoading.value = true

        val response = searcher.search()
        val nextKey = if (page + 1 < response.nbPages) params.key?.plus(1) else null

        withContext(searcher.coroutineScope.coroutineContext) {
            searcher.response.value = response
            searcher.isLoading.value = false
        }

        return Response(
            data = response.hits.map(transformer),
            page = Page(
                first = 0,
                last = response.nbPages,
                next = nextKey,
                prev = if (nextKey != null && nextKey > 0) nextKey.minus(1) else null
            )
        )
    }

}