package io.github.drumber.kitsune.data.source.jsonapi.algoliakey.api

import io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model.NetworkAlgoliaKeyCollection
import retrofit2.http.GET

interface AlgoliaKeyApi {

    @GET("algolia-keys")
    suspend fun getAllAlgoliaKeys(): NetworkAlgoliaKeyCollection

}