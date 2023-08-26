package io.github.drumber.kitsune.domain.repository

import io.github.drumber.kitsune.domain.model.infrastructure.algolia.AlgoliaKeyCollection
import io.github.drumber.kitsune.domain.service.auth.AlgoliaKeyService

class AlgoliaKeyRepository(private val algoliaService: AlgoliaKeyService) {

    private var cachedKeys: AlgoliaKeyCollection? = null

    suspend fun getAlgoliaKeys(): AlgoliaKeyCollection {
        return cachedKeys ?: algoliaService.allAlgoliaKeys().apply {
            cachedKeys = this
        }
    }

}