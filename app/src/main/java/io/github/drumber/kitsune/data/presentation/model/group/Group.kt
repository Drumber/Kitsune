package io.github.drumber.kitsune.data.presentation.model.group

data class Group(
    val id: String,
    val createdAt: String?,
    val lastActivityAt: String?,

    val name: String?,
    val slug: String?,
    val tagline: String?,
    val about: String?,

    val rules: String?,
    val rulesFormatted: String?,

    val privacy: String?,
    val nsfw: Boolean,
    val featured: Boolean,

    val membersCount: Int,
    val leadersCount: Int,

    val avatarUrl: String?,
    val coverImageUrl: String?,

    val categoryId: String?,
    val categoryName: String?
)
