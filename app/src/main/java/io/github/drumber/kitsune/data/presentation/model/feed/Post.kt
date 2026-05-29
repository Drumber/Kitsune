package io.github.drumber.kitsune.data.presentation.model.feed

data class Post(
    val id: String,
    val createdAt: String?,

    val content: String?,
    val contentFormatted: String?,

    val spoiler: Boolean,
    val nsfw: Boolean,

    val commentsCount: Int,
    val likesCount: Int,

    val authorName: String?,
    val authorAvatarUrl: String?,

    val mediaTitle: String?
)
