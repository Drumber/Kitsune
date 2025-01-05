package io.github.drumber.kitsune.data.source.jsonapi.character.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkMedia

@Type("mediaCharacters")
data class NetworkMediaCharacter(
    @Id
    val id: String?,
    val role: NetworkMediaCharacterRole?,

    @Relationship("media")
    val media: NetworkMedia?
)
