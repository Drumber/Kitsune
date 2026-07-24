package io.github.drumber.kitsune.ui.reactiondetail.compose

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover
import io.github.drumber.kitsune.util.parseUtcDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionDetailScreen(
    reaction: MediaReaction?,
    isLoading: Boolean,
    isUpvoted: Boolean,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    onNavigateUp: () -> Unit,
    onUpvote: () -> Unit,
    onMediaClick: () -> Unit,
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
                title = { Text(stringResource(R.string.title_reaction)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            isLoading && reaction == null ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            reaction != null ->
                ReactionDetailContent(
                    reaction = reaction,
                    isUpvoted = isUpvoted,
                    onUpvote = onUpvote,
                    onMediaClick = onMediaClick,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            else -> Unit
        }
    }
}

@Composable
private fun ReactionDetailContent(
    reaction: MediaReaction,
    isUpvoted: Boolean,
    onUpvote: () -> Unit,
    onMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ReactionAuthorRow(reaction = reaction)
        Spacer(Modifier.height(16.dp))
        if (!reaction.reaction.isNullOrBlank() || !reaction.content.isNullOrBlank()) {
            Text(
                text = reaction.reaction ?: reaction.content ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
        }
        if (!reaction.mediaTitle.isNullOrBlank()) {
            ReactionMediaCard(reaction = reaction, onMediaClick = onMediaClick)
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = onUpvote,
            enabled = !isUpvoted
        ) {
            Icon(
                imageVector = if (isUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = reaction.upVotesCount.toString())
        }
    }
}

@Composable
private fun ReactionAuthorRow(reaction: MediaReaction) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(imageUrl = reaction.authorAvatarUrl, size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = reaction.authorName ?: stringResource(R.string.feed_unknown_user),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val timestamp = remember(reaction.createdAt) {
                reaction.createdAt?.parseUtcDate()?.let { date ->
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
}

@Composable
private fun ReactionMediaCard(reaction: MediaReaction, onMediaClick: () -> Unit) {
    val canOpen = !reaction.mediaSlug.isNullOrBlank() && reaction.mediaIsAnime != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = canOpen, onClick = onMediaClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaCover(
            imageUrl = reaction.mediaPosterUrl,
            modifier = Modifier
                .size(width = 60.dp, height = 85.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = reaction.mediaTitle ?: "",
            style = MaterialTheme.typography.titleSmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
