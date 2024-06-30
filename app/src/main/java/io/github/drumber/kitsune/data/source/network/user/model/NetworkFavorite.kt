package io.github.drumber.kitsune.data.source.network.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("favorites")
data class NetworkFavorite(
    @Id
    val id: String? = null,
    val favRank: Int? = null,

    @Relationship("item")
    val item: NetworkFavoriteItem? = null,
    @Relationship("user")
    val user: NetworkUser? = null
)

interface NetworkFavoriteItem
