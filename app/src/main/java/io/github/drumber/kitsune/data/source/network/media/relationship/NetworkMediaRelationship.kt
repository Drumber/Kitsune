package io.github.drumber.kitsune.data.source.network.media.relationship

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.media.NetworkMedia

@Type("mediaRelationships")
data class NetworkMediaRelationship(
    @Id
    val id: String?,
    val role: NetworkMediaRelationshipRole?,

    @Relationship("destination")
    val media: NetworkMedia?
)
