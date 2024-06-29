package io.github.drumber.kitsune.domain_old.repository

import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.AlgoliaKeyCollection
import io.github.drumber.kitsune.domain_old.service.auth.AlgoliaKeyService

class AlgoliaKeyRepository(private val algoliaService: AlgoliaKeyService) {

    private var cachedKeys: AlgoliaKeyCollection? = null

    suspend fun getAlgoliaKeys(): AlgoliaKeyCollection {
        return cachedKeys ?: algoliaService.allAlgoliaKeys().apply {
            cachedKeys = this
        }
    }

}