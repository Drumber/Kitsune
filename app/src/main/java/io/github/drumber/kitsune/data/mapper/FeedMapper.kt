package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.EmbedMapper.toEmbed
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode

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
        authorId = user?.id,
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.originalOrDown(),
        mediaTitle = media?.canonicalTitle,
        mediaId = media?.id,
        mediaPosterUrl = media?.posterImage?.originalOrDown(),
        mediaSynopsis = media?.description,
        mediaSlug = media?.slug,
        mediaIsAnime = media?.let { it is NetworkAnime },
        spoiledUnitNumber = spoiledUnit?.number,
        spoiledUnitId = spoiledUnit?.id,
        spoiledUnitTitle = spoiledUnit?.canonicalTitle,
        spoiledUnitIsEpisode = spoiledUnit is NetworkEpisode,
        imageUrls = uploads
            ?.sortedBy { it.uploadOrder ?: 0 }
            ?.mapNotNull { it.content?.originalOrDown() }
            ?: emptyList(),
        uploadIds = uploads
            ?.sortedBy { it.uploadOrder ?: 0 }
            ?.mapNotNull { it.id }
            ?: emptyList(),
        embed = embed?.toEmbed()
    )
}
