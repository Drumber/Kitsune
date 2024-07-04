package io.github.drumber.kitsune.data.source.network.media.model.production

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter

@Type("castings")
data class NetworkCasting(
    @Id
    val id: String?,
    val role: String?,
    val voiceActor: Boolean?,
    val featured: Boolean?,
    val language: String?,

    @Relationship("character")
    val character: NetworkCharacter?,
    @Relationship("person")
    val person: NetworkPerson?
)
