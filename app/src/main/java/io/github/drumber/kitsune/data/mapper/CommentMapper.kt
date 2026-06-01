package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment

object CommentMapper {

    fun NetworkComment.toComment(
        isLikedByMe: Boolean = false,
        myLikeId: String? = null
    ) = Comment(
        id = id.require(),
        content = content,
        createdAt = createdAt,
        likesCount = likesCount ?: 0,
        isLikedByMe = isLikedByMe,
        myLikeId = myLikeId,
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.originalOrDown()
    )

}
