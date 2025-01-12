package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.api.CastingApi
import io.github.drumber.kitsune.data.source.jsonapi.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.source.jsonapi.toPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CastingNetworkDataSource(
    private val castingApi: CastingApi
) {

    suspend fun getAllCastings(filter: Filter): PageData<NetworkCasting> {
        return withContext(Dispatchers.IO) {
            castingApi.getAllCastings(filter.options).toPageData()
        }
    }

}