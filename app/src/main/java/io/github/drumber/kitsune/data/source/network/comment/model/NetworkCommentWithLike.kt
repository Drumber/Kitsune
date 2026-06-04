package io.github.drumber.kitsune.data.source.network.comment.model

/**
 * A network comment paired with the id of the current user's like for it, or `null` when the user
 * has not liked the comment. Lets the comment paging source carry like state through the data layer
 * without depending on presentation models.
 */
data class NetworkCommentWithLike(
    val comment: NetworkComment,
    val likeId: String?
)
