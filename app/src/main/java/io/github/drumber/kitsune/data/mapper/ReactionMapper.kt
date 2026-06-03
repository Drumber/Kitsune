package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction

object ReactionMapper {

    fun NetworkMediaReaction.toMediaReaction(): MediaReaction {
        val media: NetworkMedia? = anime ?: manga
        return MediaReaction(
            id = id.require(),
            createdAt = createdAt,
            reaction = reaction,
            content = content,
            contentFormatted = contentFormatted,
            upVotesCount = upVotesCount ?: 0,
            authorId = user?.id,
            authorName = user?.name,
            authorAvatarUrl = user?.avatar?.originalOrDown(),
            mediaId = media?.id,
            mediaTitle = media?.canonicalTitle,
            mediaPosterUrl = media?.posterImage?.originalOrDown(),
            mediaSlug = media?.slug,
            mediaIsAnime = media?.let { it is NetworkAnime }
        )
    }

}
