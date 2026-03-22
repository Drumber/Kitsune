package io.github.drumber.kitsune.data.source.jsonapi.algoliakey

import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.api.AlgoliaKeyApi
import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKeyCollection
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