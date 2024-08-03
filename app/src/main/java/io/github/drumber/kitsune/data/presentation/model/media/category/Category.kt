package io.github.drumber.kitsune.data.presentation.model.media.category

data class Category(
    val id: String,
    val slug: String?,

    val title: String?,
    val description: String?,
    val nsfw: Boolean?,

    val totalMediaCount: Int?,
    val childCount: Int?
)
