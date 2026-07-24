package io.github.drumber.kitsune.ui.postdetail.compose

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MarkdownText
import io.github.drumber.kitsune.util.parseUtcDate

@Composable
fun CommentCard(
    comment: Comment,
    isLiked: Boolean,
    likesCount: Int,
    currentUserId: String?,
    isReply: Boolean = false,
    onLikeClick: (Comment) -> Unit,
    onReplyClick: ((Comment) -> Unit)? = null,
    onViewAllRepliesClick: ((Comment) -> Unit)? = null,
    onEditClick: ((Comment) -> Unit)? = null,
    onDeleteClick: ((Comment) -> Unit)? = null,
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 40.dp else 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 4.dp
            )
    ) {
        CommentHeader(
            comment = comment,
            currentUserId = currentUserId,
            onAuthorClick = onAuthorClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
        Spacer(Modifier.height(4.dp))
        if (!comment.content.isNullOrBlank()) {
            MarkdownText(
                content = comment.contentFormatted ?: comment.content,
                isHtml = comment.contentFormatted != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(4.dp))
        CommentFooter(
            comment = comment,
            isLiked = isLiked,
            likesCount = likesCount,
            isReply = isReply,
            onLikeClick = onLikeClick,
            onReplyClick = onReplyClick
        )
        if (!isReply) {
            CommentReplies(
                comment = comment,
                currentUserId = currentUserId,
                onLikeClick = onLikeClick,
                onViewAllRepliesClick = onViewAllRepliesClick,
                onAuthorClick = onAuthorClick
            )
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun CommentHeader(
    comment: Comment,
    currentUserId: String?,
    onAuthorClick: (String) -> Unit,
    onEditClick: ((Comment) -> Unit)?,
    onDeleteClick: ((Comment) -> Unit)?
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            imageUrl = comment.authorAvatarUrl,
            size = 32.dp,
            modifier = Modifier.clickable(enabled = comment.authorId != null) {
                comment.authorId?.let(onAuthorClick)
            }
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.authorName ?: stringResource(R.string.feed_unknown_user),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(enabled = comment.authorId != null) {
                    comment.authorId?.let(onAuthorClick)
                }
            )
            val timestamp = remember(comment.createdAt) {
                comment.createdAt?.parseUtcDate()?.let { date ->
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
        val isOwner = currentUserId != null && comment.authorId == currentUserId
        if (isOwner && (onEditClick != null || onDeleteClick != null)) {
            CommentOverflowMenu(
                onEditClick = onEditClick?.let { cb -> { cb(comment) } },
                onDeleteClick = onDeleteClick?.let { cb -> { cb(comment) } }
            )
        }
    }
}

@Composable
private fun CommentOverflowMenu(
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(18.dp))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        if (onEditClick != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = { expanded = false; onEditClick() }
            )
        }
        if (onDeleteClick != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_delete)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = { expanded = false; onDeleteClick() }
            )
        }
    }
}

@Composable
private fun CommentFooter(
    comment: Comment,
    isLiked: Boolean,
    likesCount: Int,
    isReply: Boolean,
    onLikeClick: (Comment) -> Unit,
    onReplyClick: ((Comment) -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onLikeClick(comment) }, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isLiked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isReply && onReplyClick != null) {
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = { onReplyClick(comment) }, modifier = Modifier.height(32.dp)) {
                Text(
                    text = stringResource(R.string.comment_reply_hint),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun CommentReplies(
    comment: Comment,
    currentUserId: String?,
    onLikeClick: (Comment) -> Unit,
    onViewAllRepliesClick: ((Comment) -> Unit)?,
    onAuthorClick: (String) -> Unit
) {
    comment.replies.forEach { reply ->
        CommentCard(
            comment = reply,
            isLiked = reply.isLikedByMe,
            likesCount = reply.likesCount,
            currentUserId = currentUserId,
            isReply = true,
            onLikeClick = onLikeClick,
            onAuthorClick = onAuthorClick
        )
    }
    val hasMore = comment.repliesCount > comment.replies.size
    if (hasMore && onViewAllRepliesClick != null) {
        TextButton(
            onClick = { onViewAllRepliesClick(comment) },
            modifier = Modifier.padding(start = 40.dp)
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.comment_view_all_replies,
                    comment.repliesCount,
                    comment.repliesCount
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun DeleteCommentConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_comment_confirm_title)) },
        text = { Text(stringResource(R.string.delete_comment_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
