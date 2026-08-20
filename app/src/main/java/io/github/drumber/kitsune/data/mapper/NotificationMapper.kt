package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.presentation.model.feed.NotificationVerb
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkFeedSubject
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction

object NotificationMapper {

    fun NetworkActivityGroup.toNotification(): Notification {
        val activities = activities.orEmpty()
        // Activities within a group are ordered newest first.
        val latest = activities.firstOrNull()
        val actor = latest?.actor

        val resolvedPost = activities.firstNotNullOfOrNull {
            it.subject?.resolvePost() ?: it.target?.resolvePost()
        }
        val resolvedReactionId = activities.firstNotNullOfOrNull {
            it.subject?.resolveReactionId() ?: it.target?.resolveReactionId()
        }
        val excerpt = activities.firstNotNullOfOrNull {
            it.subject?.resolveExcerpt() ?: it.target?.resolveExcerpt()
        }

        return Notification(
            id = id ?: group ?: (latest?.id.orEmpty()),
            time = latest?.time ?: latest?.createdAt ?: updatedAt ?: createdAt,
            verb = NotificationVerb.fromString(latest?.verb),
            isRead = isRead ?: false,
            isSeen = isSeen ?: false,
            actorId = actor?.id,
            actorName = actor?.name,
            actorAvatarUrl = actor?.avatar?.toImage()?.largeOrDown(),
            actorCount = actorCount ?: activities.mapNotNull { it.actor?.id }.distinct().size,
            excerpt = excerpt,
            targetPost = resolvedPost,
            targetReactionId = resolvedReactionId
        )
    }

    private fun NetworkFeedSubject.resolvePost(): Post? = when (this) {
        is NetworkPost -> toPost()
        is NetworkComment -> post?.toPost()
        else -> null
    }

    private fun NetworkFeedSubject.resolveReactionId(): String? = when (this) {
        is NetworkMediaReaction -> id
        else -> null
    }

    private fun NetworkFeedSubject.resolveExcerpt(): String? = when (this) {
        is NetworkPost -> content
        is NetworkComment -> content ?: post?.content
        is NetworkMediaReaction -> content
        else -> null
    }?.trim()?.takeIf { it.isNotBlank() }

}
