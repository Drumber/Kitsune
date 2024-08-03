package io.github.drumber.kitsune.data.source.network.character.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CharacterApi {

    @GET("characters/{id}")
    suspend fun getCharacter(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<NetworkCharacter>

}