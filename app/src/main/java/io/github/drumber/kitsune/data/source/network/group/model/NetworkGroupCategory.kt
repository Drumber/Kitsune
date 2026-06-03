package io.github.drumber.kitsune.data.source.network.group.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("groupCategories")
data class NetworkGroupCategory(
    @Id
    val id: String?,

    val name: String? = null,
    val slug: String? = null,
    val description: String? = null
)
