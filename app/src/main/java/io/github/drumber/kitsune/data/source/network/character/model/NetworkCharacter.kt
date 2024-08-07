package io.github.drumber.kitsune.data.source.network.character.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavoriteItem

@Type("characters")
data class NetworkCharacter(
    @Id
    val id: String?,
    val slug: String? = null,
    val name: String? = null,
    val names: Titles? = null,
    val otherNames: List<String>? = null,
    val malId: Int? = null,
    val description: String? = null,
    val image: Image? = null,

    @Relationship("mediaCharacters")
    val mediaCharacters: List<NetworkMediaCharacter>? = null
) : NetworkFavoriteItem
