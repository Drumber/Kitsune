package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost

object FeedMapper {

    fun NetworkPost.toPost() = Post(
        id = id.require(),
        createdAt = createdAt,
        content = content,
        contentFormatted = contentFormatted,
        spoiler = spoiler ?: false,
        nsfw = nsfw ?: false,
        commentsCount = commentsCount ?: 0,
        likesCount = postLikesCount ?: 0,
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.originalOrDown(),
        mediaTitle = media?.canonicalTitle
    )

}
