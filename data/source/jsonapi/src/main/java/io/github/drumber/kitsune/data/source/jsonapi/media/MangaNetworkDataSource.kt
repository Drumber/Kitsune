package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.api.MangaApi
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.jsonapi.toPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MangaNetworkDataSource(
    private val mangaApi: MangaApi
) {

    suspend fun getAllManga(filter: Filter): PageData<NetworkManga> {
        return withContext(Dispatchers.IO) {
            mangaApi.getAllManga(filter.options).toPageData()
        }
    }

    suspend fun getManga(id: String, filter: Filter): NetworkManga? {
        return withContext(Dispatchers.IO) {
            mangaApi.getManga(id, filter.options).get()
        }
    }

    suspend fun getTrending(filter: Filter): PageData<NetworkManga> {
        return withContext(Dispatchers.IO) {
            mangaApi.getTrending(filter.options).toPageData()
        }
    }
}