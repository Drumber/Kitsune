package io.github.drumber.kitsune.ui.feed.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostInteractionStore
import io.github.drumber.kitsune.ui.navigation.LocalReselectEvents
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    globalPosts: LazyPagingItems<Post>,
    followingPosts: LazyPagingItems<Post>,
    interactionStates: Map<String, PostInteractionStore.State>,
    revealedPosts: Set<String>,
    loginRequired: Boolean,
    nsfwAllowed: Boolean,
    currentUserId: String?,
    globalSnackbarMessage: String?,
    followingSnackbarMessage: String?,
    onGlobalSnackbarShown: () -> Unit,
    onFollowingSnackbarShown: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onCreatePost: () -> Unit,
    onPostClick: (Post) -> Unit,
    onLikeClick: (Post, Boolean, Int) -> Unit,
    onRevealClick: (Post, Int) -> Unit,
    onMediaClick: (Post) -> Unit,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post, Int) -> Unit,
    onAuthorClick: (String) -> Unit,
    onRefresh: (Int) -> Unit,
    globalListState: LazyListState = rememberLazyListState(),
    followingListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { FEED_PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollToTopEvents = LocalReselectEvents.current
    val tabTitles = listOf(
        stringResource(R.string.feed_tab_global),
        stringResource(R.string.feed_tab_following)
    )

    LaunchedEffect(Unit) {
        scrollToTopEvents.collect {
            scrollBehavior.state.heightOffset = 0f
            scrollBehavior.state.contentOffset = 0f
            when (pagerState.currentPage) {
                PAGE_GLOBAL -> globalListState.animateScrollToItem(0)
                PAGE_FOLLOWING -> followingListState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                FeedTopBar(
                    onNavigateToGroups = onNavigateToGroups,
                    onNavigateToNotifications = onNavigateToNotifications,
                    scrollBehavior = scrollBehavior
                )
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_create_post))
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { page ->
            val pagePosts = if (page == PAGE_GLOBAL) globalPosts else followingPosts
            val listState = if (page == PAGE_GLOBAL) globalListState else followingListState
            FeedListScreen(
                posts = pagePosts,
                pinnedPost = null,
                loginRequired = loginRequired && page == PAGE_FOLLOWING,
                interactionStates = interactionStates,
                revealedPosts = revealedPosts,
                nsfwAllowed = nsfwAllowed,
                currentUserId = currentUserId,
                snackbarMessage = if (pagerState.currentPage == page) {
                    if (page == PAGE_GLOBAL) globalSnackbarMessage else followingSnackbarMessage
                } else {
                    null
                },
                onSnackbarShown = if (page == PAGE_GLOBAL) onGlobalSnackbarShown else onFollowingSnackbarShown,
                onRefresh = { onRefresh(page) },
                onPostClick = onPostClick,
                onLikeClick = { post, targetLiked -> onLikeClick(post, targetLiked, page) },
                onRevealClick = { post -> onRevealClick(post, page) },
                onMediaClick = onMediaClick,
                onEditClick = onEditClick,
                onDeleteClick = { post -> onDeleteClick(post, page) },
                onAuthorClick = onAuthorClick,
                lazyListState = listState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(
    onNavigateToGroups: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = { Text(stringResource(R.string.nav_feed)) },
        actions = {
            IconButton(onClick = onNavigateToGroups) {
                Icon(Icons.Default.Group, contentDescription = stringResource(R.string.action_groups))
            }
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.action_notifications)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

private const val FEED_PAGE_COUNT = 2
private const val PAGE_GLOBAL = 0
private const val PAGE_FOLLOWING = 1
