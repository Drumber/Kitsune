package io.github.drumber.kitsune.ui.details.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.ui.component.compose.media.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaFeedScreen(
    title: String,
    items: LazyPagingItems<Post>,
    onNavigateUp: () -> Unit,
    onPostClick: (Post) -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0
        KitsunePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { items.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PagingColumn(
                items = items,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                key = { it.id }
            ) { item ->
                if (item != null) {
                    PostItem(
                        post = item,
                        onClick = { onPostClick(item) },
                        onAuthorClick = onAuthorClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PostItem(
    post: Post,
    onClick: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(
                imageUrl = post.authorAvatarUrl,
                size = 36.dp,
                contentDescription = post.authorName,
                modifier = Modifier.clickable { post.authorId?.let { onAuthorClick(it) } }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = post.authorName.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(6.dp))
        val content = post.content?.takeIf { it.isNotBlank() } ?: post.contentFormatted
        if (!content.isNullOrBlank()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
