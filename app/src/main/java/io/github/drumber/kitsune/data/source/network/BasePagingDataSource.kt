package io.github.drumber.kitsune.data.source.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.util.logE

abstract class BasePagingDataSource<Value : Any> : PagingSource<Int, Value>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val pageData = requestPage(pageOffset)

            val data = pageData.data ?: throw NoDataException("Received data is 'null'.")

            LoadResult.Page(
                data = data,
                prevKey = pageData.prev,
                nextKey = pageData.next
            )
        } catch (e: Exception) {
            logE("Error receiving data from API.", e)
            LoadResult.Error(e)
        }
    }

    abstract suspend fun requestPage(pageOffset: Int): PageData<Value>

    override fun getRefreshKey(state: PagingState<Int, Value>) =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

}