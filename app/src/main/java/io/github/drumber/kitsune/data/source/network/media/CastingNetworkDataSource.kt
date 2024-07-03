package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.api.CastingApi
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.domain_old.service.Filter
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