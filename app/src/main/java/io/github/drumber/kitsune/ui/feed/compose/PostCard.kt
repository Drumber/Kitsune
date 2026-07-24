package io.github.drumber.kitsune.ui.feed.compose

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Embed
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MarkdownText
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover
import io.github.drumber.kitsune.util.parseUtcDate

@Composable
fun PostCard(
    post: Post,
    interactionState: PostInteractionStore.State?,
    isRevealed: Boolean,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean) -> Unit,
    onRevealClick: (Post) -> Unit,
    onMediaClick: (Post) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val needsWarning = post.spoiler || (post.nsfw && !nsfwAllowed)
    val gated = needsWarning && !isRevealed
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        PostCardHeader(
            post = post,
            currentUserId = currentUserId,
            onAuthorClick = onAuthorClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
        Spacer(Modifier.height(8.dp))
        if (gated) {
            PostContentWarning(
                isNsfw = post.nsfw && !post.spoiler,
                onReveal = { onRevealClick(post) }
            )
        } else {
            PostContentBody(
                post = post,
                interactionState = interactionState,
                onMediaClick = onMediaClick
            )
        }
        Spacer(Modifier.height(8.dp))
        PostCardFooter(
            post = post,
            interactionState = interactionState,
            onLikeClick = onLikeClick
        )
    }
    HorizontalDivider()
}

@Composable
private fun PostCardHeader(
    post: Post,
    currentUserId: String?,
    onAuthorClick: (String) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            imageUrl = post.authorAvatarUrl,
            size = 40.dp,
            modifier = Modifier.clickable(enabled = post.authorId != null) {
                post.authorId?.let(onAuthorClick)
            }
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.authorName ?: stringResource(R.string.feed_unknown_user),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = post.authorId != null) {
                    post.authorId?.let(onAuthorClick)
                }
            )
            val timestamp = remember(post.createdAt) {
                post.createdAt?.parseUtcDate()?.let { date ->
                    DateUtils.getRelativeTimeSpanString(
                        date.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString()
                }
            }
            if (timestamp != null) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        val isOwner = currentUserId != null && post.authorId == currentUserId
        if (isOwner) {
            PostOverflowMenu(
                onEditClick = { onEditClick(post) },
                onDeleteClick = { onDeleteClick(post) }
            )
        }
    }
}

@Composable
private fun PostOverflowMenu(onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = null)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_edit)) },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = { expanded = false; onEditClick() }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_delete)) },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = { expanded = false; onDeleteClick() }
        )
    }
}

@Composable
private fun PostContentWarning(isNsfw: Boolean, onReveal: () -> Unit) {
    FilledTonalButton(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                if (isNsfw) R.string.feed_nsfw_warning_title else R.string.feed_spoiler_warning_title
            )
        )
    }
}

@Composable
private fun PostContentBody(
    post: Post,
    interactionState: PostInteractionStore.State?,
    onMediaClick: (Post) -> Unit
) {
    if (!post.content.isNullOrBlank()) {
        MarkdownText(
            content = post.contentFormatted ?: post.content,
            isHtml = post.contentFormatted != null,
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (post.imageUrls.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        PostImagePreview(imageUrls = post.imageUrls)
    }
    val embed = post.embed
    if (embed != null && (!embed.imageUrl.isNullOrBlank() || !embed.title.isNullOrBlank())) {
        Spacer(Modifier.height(8.dp))
        PostEmbedCard(embed = embed)
    }
    if (!post.mediaTitle.isNullOrBlank()) {
        Spacer(Modifier.height(8.dp))
        PostMediaCard(post = post, onMediaClick = onMediaClick)
    }
    val likerAvatars = interactionState?.likerAvatars.orEmpty()
    if (likerAvatars.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        PostLikerAvatars(
            likerAvatars = likerAvatars,
            totalLikes = interactionState?.likesCount ?: post.likesCount
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PostImagePreview(imageUrls: List<String>) {
    GlideImage(
        model = imageUrls.first(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        it.placeholder(R.drawable.ic_insert_photo_48).error(R.drawable.ic_insert_photo_48)
    }
    if (imageUrls.size > 1) {
        Text(
            text = stringResource(R.string.feed_image_count_more, imageUrls.size - 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun PostEmbedCard(embed: Embed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (!embed.imageUrl.isNullOrBlank()) {
                MediaCover(
                    imageUrl = embed.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.height(4.dp))
            }
            if (!embed.siteName.isNullOrBlank()) {
                Text(
                    text = embed.siteName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!embed.title.isNullOrBlank()) {
                Text(text = embed.title, style = MaterialTheme.typography.titleSmall)
            }
            if (!embed.description.isNullOrBlank()) {
                Text(
                    text = embed.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PostMediaCard(post: Post, onMediaClick: (Post) -> Unit) {
    val canOpen = !post.mediaSlug.isNullOrBlank() && post.mediaIsAnime != null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canOpen) { onMediaClick(post) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            MediaCover(
                imageUrl = post.mediaPosterUrl,
                modifier = Modifier
                    .size(width = 48.dp, height = 68.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.mediaTitle ?: "",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!post.mediaSynopsis.isNullOrBlank()) {
                    Text(
                        text = post.mediaSynopsis,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PostLikerAvatars(likerAvatars: List<String>, totalLikes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        likerAvatars.take(3).forEach { url ->
            Avatar(imageUrl = url, size = 20.dp, modifier = Modifier.padding(end = 2.dp))
        }
        val remaining = totalLikes - likerAvatars.size
        if (remaining > 0) {
            Text(
                text = stringResource(R.string.feed_likers_more, remaining),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostCardFooter(
    post: Post,
    interactionState: PostInteractionStore.State?,
    onLikeClick: (Post, Boolean) -> Unit
) {
    val isLiked = interactionState?.isLiked ?: false
    val likesCount = interactionState?.likesCount ?: post.likesCount
    val commentsCount = interactionState?.commentsCount ?: post.commentsCount
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onLikeClick(post, !isLiked) }, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(
                    if (isLiked) R.string.cd_unlike_post else R.string.cd_like_post,
                    likesCount
                ),
                tint = if (isLiked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = stringResource(R.string.cd_comments_count, commentsCount),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = commentsCount.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeletePostConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_post_confirm_title)) },
        text = { Text(stringResource(R.string.delete_post_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
