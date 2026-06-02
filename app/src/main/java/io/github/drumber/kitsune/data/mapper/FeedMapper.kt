package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed
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
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.originalOrDown(),
        mediaTitle = media?.canonicalTitle,
        mediaPosterUrl = media?.posterImage?.originalOrDown(),
        mediaSynopsis = media?.description,
        mediaSlug = media?.slug,
        mediaIsAnime = media?.let { it is NetworkAnime },
        spoiledUnitNumber = spoiledUnit?.number,
        spoiledUnitTitle = spoiledUnit?.canonicalTitle,
        spoiledUnitIsEpisode = spoiledUnit is NetworkEpisode,
        imageUrls = uploads
            ?.sortedBy { it.uploadOrder ?: 0 }
            ?.mapNotNull { it.content?.originalOrDown() }
            ?: emptyList(),
        embed = embed?.toEmbed()
    )

}

fun NetworkEmbed.toEmbed(): Embed {
    val resolvedSiteName = site?.let { node ->
        if (node.isTextual) node.asText() else node.get("name")?.asText()
    }
    return Embed(
        kind = kind,
        title = title,
        description = description,
        url = url,
        siteName = resolvedSiteName,
        imageUrl = image?.url,
        videoUrl = video?.url,
        videoType = video?.type
    )
}
