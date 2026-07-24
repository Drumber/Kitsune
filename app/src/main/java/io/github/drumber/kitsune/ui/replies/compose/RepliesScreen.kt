package io.github.drumber.kitsune.ui.replies.compose

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MarkdownText
import io.github.drumber.kitsune.ui.postdetail.compose.CommentCard
import io.github.drumber.kitsune.util.parseUtcDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepliesScreen(
    parentComment: Comment?,
    parentIsLiked: Boolean,
    parentLikesCount: Int,
    replies: LazyPagingItems<Comment>,
    commentLikeOverrides: Map<String, Pair<Boolean, Int>>,
    currentUserId: String?,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    onNavigateUp: () -> Unit,
    onParentLikeClick: () -> Unit,
    onReplyLikeClick: (Comment) -> Unit,
    onAuthorClick: (String) -> Unit,
    onSubmitReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneTopAppBar(
                title = {},
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ReplyInputBar(onSubmit = onSubmitReply)
        }
    ) { innerPadding ->
        RepliesContent(
            parentComment = parentComment,
            parentIsLiked = parentIsLiked,
            parentLikesCount = parentLikesCount,
            replies = replies,
            commentLikeOverrides = commentLikeOverrides,
            currentUserId = currentUserId,
            onParentLikeClick = onParentLikeClick,
            onReplyLikeClick = onReplyLikeClick,
            onAuthorClick = onAuthorClick,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}

@Composable
private fun RepliesContent(
    parentComment: Comment?,
    parentIsLiked: Boolean,
    parentLikesCount: Int,
    replies: LazyPagingItems<Comment>,
    commentLikeOverrides: Map<String, Pair<Boolean, Int>>,
    currentUserId: String?,
    onParentLikeClick: () -> Unit,
    onReplyLikeClick: (Comment) -> Unit,
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appendState = replies.loadState.append
    LazyColumn(modifier = modifier) {
        if (parentComment != null) {
            item(key = "parent_${parentComment.id}") {
                ParentCommentHeader(
                    comment = parentComment,
                    isLiked = parentIsLiked,
                    likesCount = parentLikesCount,
                    onLikeClick = onParentLikeClick,
                    onAuthorClick = onAuthorClick
                )
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.title_replies),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        items(count = replies.itemCount, key = replies.itemKey { it.id }) { index ->
            replies[index]?.let { comment ->
                val (liked, count) = commentLikeOverrides[comment.id]
                    ?: Pair(comment.isLikedByMe, comment.likesCount)
                CommentCard(
                    comment = comment,
                    isLiked = liked,
                    likesCount = count,
                    currentUserId = currentUserId,
                    isReply = true,
                    onLikeClick = onReplyLikeClick,
                    onAuthorClick = onAuthorClick
                )
            }
        }
        when (appendState) {
            is LoadState.Loading -> item {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            is LoadState.Error -> item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.error_resource_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { replies.retry() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun ParentCommentHeader(
    comment: Comment,
    isLiked: Boolean,
    likesCount: Int,
    onLikeClick: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                imageUrl = comment.authorAvatarUrl,
                size = 40.dp,
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
        }
        if (!comment.content.isNullOrBlank()) {
            MarkdownText(
                content = comment.contentFormatted ?: comment.content,
                isHtml = comment.contentFormatted != null,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            IconButton(onClick = onLikeClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isLiked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = likesCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReplyInputBar(onSubmit: (String) -> Unit) {
    var inputText by rememberSaveable { mutableStateOf("") }
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.comment_reply_hint)) },
                maxLines = 4,
                singleLine = false
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { if (inputText.isNotBlank()) { onSubmit(inputText.trim()); inputText = "" } },
                enabled = inputText.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}
