package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.model.auth.AlgoliaKeyCollection
import io.github.drumber.kitsune.data.service.auth.AlgoliaKeyService

class AlgoliaKeyRepository(private val algoliaService: AlgoliaKeyService) {

    private var cachedKeys: AlgoliaKeyCollection? = null

    suspend fun getAlgoliaKeys(): AlgoliaKeyCollection {
        return cachedKeys ?: algoliaService.allAlgoliaKeys().apply {
            cachedKeys = this
        }
    }

}