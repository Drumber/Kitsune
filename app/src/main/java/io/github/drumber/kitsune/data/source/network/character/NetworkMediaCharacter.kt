package io.github.drumber.kitsune.data.source.network.character

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.media.NetworkMedia

@Type("mediaCharacters")
data class NetworkMediaCharacter(
    @Id
    val id: String?,
    val role: NetworkMediaCharacterRole?,

    @Relationship("media")
    val media: NetworkMedia?
)
