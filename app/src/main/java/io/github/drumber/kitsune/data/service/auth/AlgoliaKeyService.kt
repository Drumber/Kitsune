package io.github.drumber.kitsune.data.service.auth

import io.github.drumber.kitsune.data.model.auth.AlgoliaKeyCollection
import retrofit2.http.GET

interface AlgoliaKeyService {

    @GET("algolia-keys")
    suspend fun allAlgoliaKeys(): AlgoliaKeyCollection

}