package io.github.drumber.kitsune.data.presentation.model.reaction

data class MediaReaction(
    val id: String,
    val createdAt: String?,

    val reaction: String?,
    val content: String?,
    val contentFormatted: String?,

    val upVotesCount: Int,

    val authorId: String?,
    val authorName: String?,
    val authorAvatarUrl: String?,

    val mediaId: String?,
    val mediaTitle: String?,
    val mediaPosterUrl: String?,
    val mediaSlug: String?,
    val mediaIsAnime: Boolean?
)
