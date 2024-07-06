package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.api.AnimeApi
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.data.common.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnimeNetworkDataSource(
    private val animeApi: AnimeApi
) {

    suspend fun getAllAnime(filter: Filter): PageData<NetworkAnime> {
        return withContext(Dispatchers.IO) {
            animeApi.getAllAnime(filter.options).toPageData()
        }
    }

    suspend fun getAnime(id: String, filter: Filter): NetworkAnime? {
        return withContext(Dispatchers.IO) {
            animeApi.getAnime(id, filter.options).get()
        }
    }

    suspend fun getTrending(filter: Filter): PageData<NetworkAnime> {
        return withContext(Dispatchers.IO) {
            animeApi.getTrending(filter.options).toPageData()
        }
    }

    suspend fun getLanguages(id: String): List<String> {
        return withContext(Dispatchers.IO) {
            animeApi.getLanguages(id)
        }
    }
}