package io.github.drumber.kitsune.data.presentation.model.reaction

data class MediaReaction(
    val id: String,
    val createdAt: String?,

    val reaction: String?,
    val content: String?,

    val upVotesCount: Int,

    val authorName: String?,
    val authorAvatarUrl: String?
)
