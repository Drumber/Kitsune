package io.github.drumber.kitsune.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain.model.toPage
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE

abstract class BasePagingDataSource<Value : Any>(
    protected val filter: Filter
): PagingSource<Int, Value>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val response = requestService(filter.pageOffset(pageOffset))

            val data = response.get() ?: throw ReceivedDataException("Received data is 'null'.")
            val page = response.links?.toPage()

            LoadResult.Page(
                data = data,
                prevKey = page?.prev,
                nextKey = page?.next
            )
        } catch (e: Exception) {
            logE("Error receiving data from service.", e)
            LoadResult.Error(e)
        }
    }

    abstract suspend fun requestService(filter: Filter): JSONAPIDocument<List<Value>>

    override fun getRefreshKey(state: PagingState<Int, Value>) = state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
    }

}