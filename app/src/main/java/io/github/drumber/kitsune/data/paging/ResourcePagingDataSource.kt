package io.github.drumber.kitsune.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.Page
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE

abstract class ResourcePagingDataSource<Value : Any>(
    private val filter: Filter,
    private val requestType: RequestType
): PagingSource<Int, Value>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val response = requestResource(filter.pageOffset(pageOffset), requestType, params)

            val data = response.data ?: throw ReceivedDataException("Received data is 'null'.")
            val page = response.page

            LoadResult.Page(
                data = data,
                prevKey = page?.prev,
                nextKey = page?.next
            )
        } catch (e: Exception) {
            logE("Error receiving resource data.", e)
            LoadResult.Error(e)
        }
    }

    abstract suspend fun requestResource(filter: Filter, requestType: RequestType, params: LoadParams<Int>): Response

    override fun getRefreshKey(state: PagingState<Int, Value>) = state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?:
        state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
    }

    inner class Response(val data: List<Value>?, val page: Page?)

}