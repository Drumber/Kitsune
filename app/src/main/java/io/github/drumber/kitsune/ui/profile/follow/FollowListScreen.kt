package io.github.drumber.kitsune.ui.profile.follow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.FollowUser
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    modifier: Modifier = Modifier,
    title: String,
    users: LazyPagingItems<FollowUser>,
    followStates: Map<String, FollowButtonState> = emptyMap(),
    onNavigateUp: () -> Unit,
    onUserClick: (String) -> Unit,
    onFollowClick: (String) -> Unit,
    onResolveFollowState: (String) -> Unit,
    showButtonFor: (String) -> Boolean
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val isRefreshing = users.loadState.refresh is LoadState.Loading &&
            users.itemCount > 0

        KitsunePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { users.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PagingColumn(
                items = users,
                modifier = Modifier.fillMaxSize(),
                key = { it.userId },
                emptyContent = {
                    PagingEmptyContent(message = stringResource(R.string.follow_list_empty))
                }
            ) { user ->
                FollowUserRow(
                    followUser = user,
                    followState = user?.let { followStates[it.userId] },
                    showButton = user?.let { showButtonFor(it.userId) } ?: false,
                    onClick = { user?.let { onUserClick(it.userId) } },
                    onFollowClick = { user?.let { onFollowClick(it.userId) } },
                    onResolveFollowState = { user?.let { onResolveFollowState(it.userId) } }
                )
            }
        }
    }
}

// region Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Follow list — empty")
@Composable
private fun FollowListScreenEmptyPreview() {
    KitsuneTheme {
        val emptyItems = flowOf(PagingData.empty<FollowUser>()).collectAsLazyPagingItems()
        FollowListScreen(
            title = "Following",
            users = emptyItems,
            followStates = emptyMap(),
            onNavigateUp = {},
            onUserClick = {},
            onFollowClick = {},
            onResolveFollowState = {},
            showButtonFor = { false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Follow list — with items")
@Composable
private fun FollowListScreenWithItemsPreview() {
    KitsuneTheme {
        val items = flowOf(
            PagingData.from(
                listOf(
                    FollowUser(
                        followId = "f1",
                        userId = "u1",
                        name = "Alice",
                        slug = "alice",
                        title = "Moderator",
                        avatarUrl = null
                    ),
                    FollowUser(
                        followId = "f2",
                        userId = "u2",
                        name = "Bob",
                        slug = "bob",
                        title = null,
                        avatarUrl = null
                    )
                )
            )
        ).collectAsLazyPagingItems()
        FollowListScreen(
            title = "Following",
            users = items,
            followStates = mapOf(
                "u1" to FollowButtonState(isResolved = true, isFollowing = true),
                "u2" to FollowButtonState(isResolved = true, isFollowing = false)
            ),
            onNavigateUp = {},
            onUserClick = {},
            onFollowClick = {},
            onResolveFollowState = {},
            showButtonFor = { true }
        )
    }
}

// endregion
