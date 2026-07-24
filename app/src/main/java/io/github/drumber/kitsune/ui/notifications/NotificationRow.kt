package io.github.drumber.kitsune.ui.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.presentation.model.feed.NotificationVerb
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.util.parseUtcDate

@Composable
fun NotificationRow(
    modifier: Modifier = Modifier,
    notification: Notification?,
    onClick: () -> Unit = {}
) {
    if (notification == null) {
        NotificationRowPlaceholder(modifier = modifier)
        return
    }

    val actorDisplay = buildActorDisplay(notification)
    val summary = summaryFor(notification.verb, actorDisplay)
    val timestamp = remember(notification.time) {
        notification.time?.parseUtcDate()?.let { date ->
            DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Unread indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .then(
                    if (!notification.isRead) {
                        Modifier
                    } else {
                        Modifier
                    }
                )
        ) {
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    content = {}
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Avatar(
            imageUrl = notification.actorAvatarUrl,
            size = 40.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!notification.excerpt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (timestamp != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timestamp.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NotificationRowPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Avatar(imageUrl = null, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
private fun buildActorDisplay(notification: Notification): String {
    val name = notification.actorName ?: stringResource(R.string.feed_unknown_user)
    val others = notification.actorCount - 1
    return if (others > 0) {
        pluralStringResource(R.plurals.notification_actor_and_others, others, name, others)
    } else {
        name
    }
}

@Composable
private fun summaryFor(verb: NotificationVerb, actorDisplay: String): String {
    val resId = when (verb) {
        NotificationVerb.FOLLOWED -> R.string.notification_followed
        NotificationVerb.LIKED_POST -> R.string.notification_liked_post
        NotificationVerb.COMMENTED -> R.string.notification_commented
        NotificationVerb.REPLIED -> R.string.notification_replied
        NotificationVerb.LIKED_COMMENT -> R.string.notification_liked_comment
        NotificationVerb.LIKED_REACTION -> R.string.notification_liked_reaction
        NotificationVerb.MENTIONED -> R.string.notification_mentioned
        NotificationVerb.POSTED -> R.string.notification_posted
        NotificationVerb.AIRED -> R.string.notification_aired
        NotificationVerb.OTHER -> R.string.notification_generic
    }
    return stringResource(resId, actorDisplay)
}

// region Previews

@Preview(showBackground = true, name = "Notification row — unread")
@Composable
private fun NotificationRowUnreadPreview() {
    KitsuneTheme {
        NotificationRow(
            notification = Notification(
                id = "1",
                time = null,
                verb = NotificationVerb.FOLLOWED,
                isRead = false,
                actorName = "SomeUser",
                actorAvatarUrl = null,
                actorCount = 1,
                excerpt = null,
                targetPost = null
            )
        )
    }
}

@Preview(showBackground = true, name = "Notification row — read with excerpt")
@Composable
private fun NotificationRowReadWithExcerptPreview() {
    KitsuneTheme {
        NotificationRow(
            notification = Notification(
                id = "2",
                time = null,
                verb = NotificationVerb.COMMENTED,
                isRead = true,
                actorName = "AnotherUser",
                actorAvatarUrl = null,
                actorCount = 3,
                excerpt = "This is an excerpt of the related post content.",
                targetPost = null
            )
        )
    }
}

@Preview(showBackground = true, name = "Notification row — placeholder")
@Composable
private fun NotificationRowPlaceholderPreview() {
    KitsuneTheme {
        NotificationRow(notification = null)
    }
}

// endregion
