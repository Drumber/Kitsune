package io.github.drumber.kitsune.ui.notifications

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.data.presentation.model.feed.NotificationVerb
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    notifications: LazyPagingItems<Notification>,
    loginRequired: Boolean,
    onNavigateUp: () -> Unit,
    onNotificationClick: (Notification) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneTopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (loginRequired) {
            LoginRequiredContent(modifier = Modifier.padding(innerPadding))
        } else {
            val isRefreshing = notifications.loadState.refresh is LoadState.Loading &&
                notifications.itemCount > 0

            KitsunePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { notifications.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                PagingColumn(
                    items = notifications,
                    modifier = Modifier.fillMaxSize(),
                    key = { it.id },
                    emptyContent = {
                        PagingEmptyContent(
                            message = stringResource(R.string.error_nothing_found)
                        )
                    }
                ) { notification ->
                    NotificationRow(
                        notification = notification,
                        onClick = { notification?.let(onNotificationClick) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginRequiredContent(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.notifications_login_required),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}

// region Previews

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Notifications — login required")
@Composable
private fun NotificationsLoginRequiredPreview() {
    KitsuneTheme {
        val emptyItems = flowOf(PagingData.empty<Notification>()).collectAsLazyPagingItems()
        NotificationsScreen(
            notifications = emptyItems,
            loginRequired = true,
            onNavigateUp = {},
            onNotificationClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Notifications — empty")
@Composable
private fun NotificationsEmptyPreview() {
    KitsuneTheme {
        val emptyItems = flowOf(PagingData.empty<Notification>()).collectAsLazyPagingItems()
        NotificationsScreen(
            notifications = emptyItems,
            loginRequired = false,
            onNavigateUp = {},
            onNotificationClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Notifications — with items")
@Composable
private fun NotificationsWithItemsPreview() {
    KitsuneTheme {
        val sampleItems = flowOf(
            PagingData.from(
                listOf(
                    Notification(
                        id = "1",
                        time = null,
                        verb = NotificationVerb.FOLLOWED,
                        isRead = false,
                        actorName = "UserAlpha",
                        actorAvatarUrl = null,
                        actorCount = 1,
                        excerpt = null,
                        targetPost = null
                    ),
                    Notification(
                        id = "2",
                        time = null,
                        verb = NotificationVerb.COMMENTED,
                        isRead = true,
                        actorName = "UserBeta",
                        actorAvatarUrl = null,
                        actorCount = 2,
                        excerpt = "Great post about anime!",
                        targetPost = null
                    )
                )
            )
        ).collectAsLazyPagingItems()
        NotificationsScreen(
            notifications = sampleItems,
            loginRequired = false,
            onNavigateUp = {},
            onNotificationClick = {}
        )
    }
}

// endregion
