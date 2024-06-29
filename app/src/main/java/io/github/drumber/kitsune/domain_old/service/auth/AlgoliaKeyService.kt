package io.github.drumber.kitsune.domain_old.service.auth

import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.AlgoliaKeyCollection
import retrofit2.http.GET

interface AlgoliaKeyService {

    @GET("algolia-keys")
    suspend fun allAlgoliaKeys(): AlgoliaKeyCollection

}