package io.github.drumber.kitsune.data.presentation.model.comment

data class Comment(
    val id: String,
    val content: String?,
    val createdAt: String?,

    val likesCount: Int,
    val isLikedByMe: Boolean,
    val myLikeId: String?,

    val authorName: String?,
    val authorAvatarUrl: String?
)
