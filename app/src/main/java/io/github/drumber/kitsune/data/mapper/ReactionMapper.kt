package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction

object ReactionMapper {

    fun NetworkMediaReaction.toMediaReaction() = MediaReaction(
        id = id.require(),
        createdAt = createdAt,
        reaction = reaction,
        content = content,
        upVotesCount = upVotesCount ?: 0,
        authorName = user?.name,
        authorAvatarUrl = user?.avatar?.originalOrDown()
    )

}
