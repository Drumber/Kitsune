package io.github.drumber.kitsune.ui.profile.follow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.FollowUser
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun FollowUserRow(
    modifier: Modifier = Modifier,
    followUser: FollowUser?,
    followState: FollowButtonState?,
    showButton: Boolean,
    onClick: () -> Unit = {},
    onFollowClick: () -> Unit = {},
    onResolveFollowState: () -> Unit = {}
) {
    if (followUser == null) {
        FollowUserRowPlaceholder(modifier = modifier)
        return
    }

    LaunchedEffect(followUser.userId) {
        if (showButton) {
            onResolveFollowState()
        }
    }

    val state = followState ?: FollowButtonState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(imageUrl = followUser.avatarUrl, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = followUser.name ?: stringResource(R.string.feed_unknown_user),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!followUser.title.isNullOrBlank()) {
                Text(
                    text = followUser.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showButton) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onFollowClick,
                enabled = !state.isProcessing && state.isResolved
            ) {
                Text(
                    if (state.isFollowing) {
                        stringResource(R.string.action_unfollow)
                    } else {
                        stringResource(R.string.action_follow)
                    }
                )
            }
        }
    }
}

@Composable
private fun FollowUserRowPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(imageUrl = null, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

// region Previews

@Preview(showBackground = true, name = "Follow user row — following")
@Composable
private fun FollowUserRowFollowingPreview() {
    KitsuneTheme {
        FollowUserRow(
            followUser = FollowUser(
                followId = "f1",
                userId = "u1",
                name = "Alice",
                slug = "alice",
                title = "Moderator",
                avatarUrl = null
            ),
            followState = FollowButtonState(isResolved = true, isFollowing = true),
            showButton = true
        )
    }
}

@Preview(showBackground = true, name = "Follow user row — not following")
@Composable
private fun FollowUserRowNotFollowingPreview() {
    KitsuneTheme {
        FollowUserRow(
            followUser = FollowUser(
                followId = "f1",
                userId = "u1",
                name = "Bob",
                slug = "bob",
                title = null,
                avatarUrl = null
            ),
            followState = FollowButtonState(isResolved = true, isFollowing = false),
            showButton = true
        )
    }
}

@Preview(showBackground = true, name = "Follow user row — button hidden")
@Composable
private fun FollowUserRowButtonHiddenPreview() {
    KitsuneTheme {
        FollowUserRow(
            followUser = FollowUser(
                followId = "f1",
                userId = "u1",
                name = "Current User",
                slug = "currentuser",
                title = null,
                avatarUrl = null
            ),
            followState = null,
            showButton = false
        )
    }
}

// endregion
