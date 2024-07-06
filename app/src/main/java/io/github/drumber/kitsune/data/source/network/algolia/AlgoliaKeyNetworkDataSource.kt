package io.github.drumber.kitsune.data.source.network.algolia

import io.github.drumber.kitsune.data.source.network.algolia.api.AlgoliaKeyApi
import io.github.drumber.kitsune.data.source.network.algolia.model.NetworkAlgoliaKeyCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlgoliaKeyNetworkDataSource(
    private val algoliaKeyApi: AlgoliaKeyApi
) {

    suspend fun getAllAlgoliaKeys(): NetworkAlgoliaKeyCollection {
        return withContext(Dispatchers.IO) {
            algoliaKeyApi.getAllAlgoliaKeys()
        }
    }
}