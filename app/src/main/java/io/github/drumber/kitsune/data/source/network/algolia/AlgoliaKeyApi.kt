package io.github.drumber.kitsune.data.source.network.algolia

import retrofit2.http.GET

interface AlgoliaKeyApi {

    @GET("algolia-keys")
    suspend fun getAllAlgoliaKeys(): NetworkAlgoliaKeyCollection

}