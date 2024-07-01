package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.AlgoliaMapper.toAlgoliaKeyCollection
import io.github.drumber.kitsune.data.presentation.model.algolia.AlgoliaKeyCollection
import io.github.drumber.kitsune.data.source.network.algolia.AlgoliaKeyNetworkDataSource

class AlgoliaKeyRepository(
    private val remoteAlgoliaKeyDataSource: AlgoliaKeyNetworkDataSource
) {

    private var cache: AlgoliaKeyCollection? = null

    suspend fun getAllAlgoliaKeys(): AlgoliaKeyCollection {
        return cache
            ?: remoteAlgoliaKeyDataSource.getAllAlgoliaKeys().toAlgoliaKeyCollection().also {
                cache = it
            }
    }

}