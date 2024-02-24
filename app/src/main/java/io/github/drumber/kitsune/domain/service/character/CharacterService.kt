package io.github.drumber.kitsune.domain.service.character

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.character.Character
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface CharacterService {

    @GET("characters/{id}")
    suspend fun getCharacter(
        @Path("id") id: String,
        @QueryMap filter: Map<String, String> = emptyMap()
    ): JSONAPIDocument<Character>

}