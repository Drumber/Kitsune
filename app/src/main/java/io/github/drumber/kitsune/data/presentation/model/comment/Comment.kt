package io.github.drumber.kitsune.data.presentation.model.comment

import io.github.drumber.kitsune.data.presentation.model.feed.Embed

data class Comment(
    val id: String,
    val content: String?,
    val contentFormatted: String?,
    val createdAt: String?,

    val likesCount: Int,
    val isLikedByMe: Boolean,
    val myLikeId: String?,

    val repliesCount: Int,

    val authorId: String?,
    val authorSlug: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,

    val imageUrl: String?,
    val embed: Embed?,

    /** Bounded preview of replies loaded with the comment. May be shorter than [repliesCount]. */
    val replies: List<Comment> = emptyList()
)
