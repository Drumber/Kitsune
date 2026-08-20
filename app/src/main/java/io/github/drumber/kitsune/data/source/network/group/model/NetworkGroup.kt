package io.github.drumber.kitsune.data.source.network.group.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.NetworkImage

@Type("groups")
data class NetworkGroup(
    @Id
    val id: String?,

    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastActivityAt: String? = null,

    val name: String? = null,
    val slug: String? = null,
    val tagline: String? = null,
    val about: String? = null,
    val locale: String? = null,

    val rules: String? = null,
    val rulesFormatted: String? = null,

    val privacy: String? = null,
    val nsfw: Boolean? = null,
    val featured: Boolean? = null,

    val membersCount: Int? = null,
    val leadersCount: Int? = null,
    val neighborsCount: Int? = null,

    val avatar: NetworkImage? = null,
    val coverImage: NetworkImage? = null,

    @Relationship("category")
    val category: NetworkGroupCategory? = null
)
