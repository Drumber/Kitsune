package io.github.drumber.kitsune.data.source.jsonapi.media.model.category

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("categories")
data class NetworkCategory(
    @Id
    val id: String?,
    val slug: String?,

    val title: String?,
    val description: String?,
    val nsfw: Boolean?,

    val totalMediaCount: Int?,
    val childCount: Int?
)
