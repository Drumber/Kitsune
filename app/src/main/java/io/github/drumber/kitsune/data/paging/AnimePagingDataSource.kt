package io.github.drumber.kitsune.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.toPage
import io.github.drumber.kitsune.data.service.AnimeService
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE

class AnimePagingDataSource(
    private val service: AnimeService,
    private val filter: Filter
) : PagingSource<Int, Anime>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Anime> {
        return try {
            val pageOffset = params.key ?: Kitsu.DEFAULT_PAGE_OFFSET
            val response = service.allAnime(
                filter
                    .pageOffset(pageOffset)
                    .options
            )

            val data = response.get() ?: throw ReceivedDataException("Received data is 'null'.")
            val page = response.links?.toPage()

            LoadResult.Page(
                data = data,
                prevKey = page?.prev,
                nextKey = page?.next
            )
        } catch (e: Exception) {
            logE("Error receiving anime data.", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Anime>) = state.anchorPosition

}