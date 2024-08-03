package io.github.drumber.kitsune.data.source.network.algolia.api

import io.github.drumber.kitsune.data.source.network.algolia.model.NetworkAlgoliaKeyCollection
import retrofit2.http.GET

interface AlgoliaKeyApi {

    @GET("algolia-keys")
    suspend fun getAllAlgoliaKeys(): NetworkAlgoliaKeyCollection

}