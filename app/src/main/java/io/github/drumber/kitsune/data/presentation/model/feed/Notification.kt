package io.github.drumber.kitsune.data.presentation.model.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class NotificationVerb {
    FOLLOWED,
    LIKED_POST,
    COMMENTED,
    REPLIED,
    LIKED_COMMENT,
    LIKED_REACTION,
    MENTIONED,
    POSTED,
    AIRED,
    OTHER;

    companion object {
        fun fromString(verb: String?): NotificationVerb = when (verb?.lowercase()) {
            "follow", "followed" -> FOLLOWED
            "post_like", "like", "liked" -> LIKED_POST
            "comment", "commented" -> COMMENTED
            "reply", "replied" -> REPLIED
            "comment_like" -> LIKED_COMMENT
            "vote", "reaction_like", "like_reaction", "media_reaction_like" -> LIKED_REACTION
            "mention", "mentioned" -> MENTIONED
            "post", "posted" -> POSTED
            "media", "aired" -> AIRED
            else -> OTHER
        }
    }
}

@Parcelize
data class Notification(
    val id: String,
    val time: String?,
    val verb: NotificationVerb,
    val isRead: Boolean,
    val isSeen: Boolean,

    val actorId: String?,
    val actorName: String?,
    val actorAvatarUrl: String?,
    /** Number of distinct actors aggregated into this notification group. */
    val actorCount: Int,

    /** Short excerpt of the related post used as supporting context, if any. */
    val excerpt: String?,

    /** The post this notification relates to, if it can be resolved. Used for navigation. */
    val targetPost: Post?,

    /** The id of the media reaction this notification relates to, if any. Used for navigation. */
    val targetReactionId: String? = null
) : Parcelable
