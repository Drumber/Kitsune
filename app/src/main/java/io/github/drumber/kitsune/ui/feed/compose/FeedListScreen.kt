package io.github.drumber.kitsune.ui.feed.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingErrorContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingLoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(
    posts: LazyPagingItems<Post>,
    pinnedPost: Post?,
    loginRequired: Boolean,
    interactionStates: Map<String, PostInteractionStore.State>,
    revealedPosts: Set<String>,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    snackbarMessage: String?,
    onSnackbarShown: () -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean) -> Unit,
    onRevealClick: (Post) -> Unit,
    onMediaClick: (Post) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onAuthorClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (loginRequired) {
            FeedLoginRequiredContent(modifier = Modifier.padding(padding))
        } else {
            val refreshing = posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0
            KitsunePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                FeedPostColumn(
                    posts = posts,
                    pinnedPost = pinnedPost,
                    interactionStates = interactionStates,
                    revealedPosts = revealedPosts,
                    nsfwAllowed = nsfwAllowed,
                    currentUserId = currentUserId,
                    onPostClick = onPostClick,
                    onLikeClick = onLikeClick,
                    onRevealClick = onRevealClick,
                    onMediaClick = onMediaClick,
                    onEditClick = onEditClick,
                    onDeleteRequest = { postToDelete = it },
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }

    postToDelete?.let { pending ->
        DeletePostConfirmDialog(
            onConfirm = { onDeleteClick(pending); postToDelete = null },
            onDismiss = { postToDelete = null }
        )
    }
}

@Composable
private fun FeedLoginRequiredContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.feed_login_required),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Composable
private fun FeedPostColumn(
    posts: LazyPagingItems<Post>,
    pinnedPost: Post?,
    interactionStates: Map<String, PostInteractionStore.State>,
    revealedPosts: Set<String>,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean) -> Unit,
    onRevealClick: (Post) -> Unit,
    onMediaClick: (Post) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteRequest: (Post) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val refreshState = posts.loadState.refresh
    val appendState = posts.loadState.append
    val hasPinnedPost = pinnedPost != null
    val effectiveCount = posts.itemCount + (if (hasPinnedPost) 1 else 0)

    when {
        refreshState is LoadState.Loading && effectiveCount == 0 ->
            PagingLoadingContent(modifier = Modifier.fillMaxSize())
        refreshState is LoadState.Error && effectiveCount == 0 ->
            PagingErrorContent(modifier = Modifier.fillMaxSize(), onRetry = { posts.retry() })
        refreshState is LoadState.NotLoading &&
                appendState.endOfPaginationReached &&
                effectiveCount == 0 ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PagingEmptyContent()
            }
        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (hasPinnedPost) {
                item(key = "pinned_${pinnedPost!!.id}") {
                    PostCard(
                        post = pinnedPost,
                        interactionState = interactionStates[pinnedPost.id],
                        isRevealed = pinnedPost.id in revealedPosts,
                        nsfwAllowed = nsfwAllowed,
                        currentUserId = currentUserId,
                        onPostClick = onPostClick,
                        onLikeClick = onLikeClick,
                        onRevealClick = onRevealClick,
                        onMediaClick = onMediaClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteRequest,
                        onAuthorClick = onAuthorClick
                    )
                }
            }
            items(count = posts.itemCount, key = posts.itemKey { it.id }) { index ->
                posts[index]?.let { post ->
                    PostCard(
                        post = post,
                        interactionState = interactionStates[post.id],
                        isRevealed = post.id in revealedPosts,
                        nsfwAllowed = nsfwAllowed,
                        currentUserId = currentUserId,
                        onPostClick = onPostClick,
                        onLikeClick = onLikeClick,
                        onRevealClick = onRevealClick,
                        onMediaClick = onMediaClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteRequest,
                        onAuthorClick = onAuthorClick
                    )
                }
            }
            when (appendState) {
                is LoadState.Loading -> item { FeedAppendLoading() }
                is LoadState.Error -> item { FeedAppendError(onRetry = { posts.retry() }) }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

@Composable
private fun FeedAppendLoading() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun FeedAppendError(onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_resource_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}
