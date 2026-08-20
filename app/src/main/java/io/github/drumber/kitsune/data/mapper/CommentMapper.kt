package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.EmbedMapper.toEmbed
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment

object CommentMapper {

    fun NetworkComment.toComment(
        isLikedByMe: Boolean = false,
        myLikeId: String? = null
    ) = Comment(
        id = id.require(),
        content = content,
        contentFormatted = contentFormatted,
        createdAt = createdAt,
        likesCount = likesCount ?: 0,
        isLikedByMe = isLikedByMe,
        myLikeId = myLikeId,
        repliesCount = repliesCount ?: 0,
        authorId = user?.id,
        authorSlug = user?.slug,
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.toImage()?.largeOrDown(),
        imageUrl = uploads
            ?.sortedBy { it.uploadOrder ?: 0 }
            ?.firstNotNullOfOrNull { it.content?.toImage()?.largeOrDown() },
        embed = embed?.toEmbed()
    )

}
