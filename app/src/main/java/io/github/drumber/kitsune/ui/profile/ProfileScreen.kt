package io.github.drumber.kitsune.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Stateless profile screen composable for both MyProfile and UserProfile destinations.
 *
 * The [aboutTabContent] and [feedTabContent] slots receive the actual tab body as composable
 * lambdas — the caller (Fragment) owns the navigation callbacks and ViewModel access, keeping
 * this screen free of Android framework types.
 *
 * **Tab content strategy:**
 * - Tab 0 — About: [ProfileAboutScreen] is called directly by the hosting Fragment; no child
 *   Fragment is needed because it is already a pure composable.
 * - Tab 1 — Feed: The hosting Fragment inlines [FeedListViewModel] + [FeedListScreen] in a
 *   private composable, avoiding a `FragmentContainerView` embed and the associated lifecycle
 *   complexity. Navigation callbacks are captured from the Fragment scope.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    displayName: String,
    subtitle: String?,
    isMyProfile: Boolean,
    uiState: ProfileUiState,
    scrollToTopEvents: Flow<Unit>,
    aboutTabContent: @Composable (LazyListState) -> Unit,
    feedTabContent: @Composable (LazyListState) -> Unit,
    onShareProfile: () -> Unit,
    onPostOnWall: () -> Unit,
    onCoverClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogOut: () -> Unit,
    onSignIn: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val aboutScrollState = rememberLazyListState()
    val feedScrollState = rememberLazyListState()

    val tabTitles = listOf(
        stringResource(R.string.profile_tab_about),
        stringResource(R.string.profile_tab_posts)
    )

    // Expand the app bar and scroll the visible tab's content to the top on nav-bar reselect.
    LaunchedEffect(Unit) {
        scrollToTopEvents.collect {
            scrollBehavior.state.heightOffset = 0f
            scrollBehavior.state.contentOffset = 0f
            when (pagerState.currentPage) {
                0 -> aboutScrollState.animateScrollToItem(0)
                1 -> feedScrollState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ProfileTopBar(
                user = user,
                displayName = displayName,
                subtitle = subtitle,
                isMyProfile = isMyProfile,
                collapsedFraction = scrollBehavior.state.collapsedFraction,
                scrollBehavior = scrollBehavior,
                onShareProfile = onShareProfile,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onLogOut = onLogOut,
                onNavigateUp = onNavigateUp,
                onCoverClick = onCoverClick,
                onAvatarClick = onAvatarClick
            )
        },
        floatingActionButton = {
            // FAB is shown on the Posts tab once a user profile is loaded.
            if (user != null && pagerState.currentPage == 1) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.profile_post_on_wall)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    },
                    onClick = onPostOnWall
                )
            }
        }
    ) { paddingValues ->
        when {
            // Show loading spinner only on initial load before any user data arrives.
            uiState.isInitialLoading && user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // "Not logged in" state is only possible on the My Profile tab.
            isMyProfile && user == null -> {
                NotLoggedInContent(
                    onSignIn = onSignIn,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = { Text(title) }
                            )
                        }
                    }
                    // Keep both pages alive to preserve scroll position when switching tabs.
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                        beyondViewportPageCount = 1
                    ) { page ->
                        when (page) {
                            0 -> aboutTabContent(aboutScrollState)
                            1 -> feedTabContent(feedScrollState)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    user: User?,
    displayName: String,
    subtitle: String?,
    isMyProfile: Boolean,
    collapsedFraction: Float,
    scrollBehavior: TopAppBarScrollBehavior,
    onShareProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogOut: () -> Unit,
    onNavigateUp: () -> Unit,
    onCoverClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        // Cover image sits behind the toolbar; fades out as the bar collapses.
        AsyncImage(
            model = user?.coverImage?.originalOrDown(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 1f - collapsedFraction }
                .clickable(onClick = onCoverClick, enabled = user?.coverImage != null)
        )

        LargeTopAppBar(
            title = {
                Column {
                    Text(
                        text = displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Slug fades away as the toolbar collapses into the compact bar.
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.graphicsLayer {
                                alpha = (1f - collapsedFraction * 2f).coerceIn(0f, 1f)
                            }
                        )
                    }
                }
            },
            navigationIcon = {
                if (isMyProfile) {
                    // For "My Profile", the avatar acts as the navigation logo (no back button).
                    IconButton(onClick = onAvatarClick, enabled = user?.avatar != null) {
                        Avatar(
                            imageUrl = user?.avatar?.originalOrDown(),
                            size = 32.dp,
                            contentDescription = user?.name
                        )
                    }
                } else {
                    KitsuneBackButton(onNavigateUp = onNavigateUp)
                }
            },
            actions = {
                if (isMyProfile && user != null) {
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.action_edit_profile)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                }
                // Overflow menu (share + logout).
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (user != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share_profile_url)) },
                                onClick = { menuExpanded = false; onShareProfile() }
                            )
                        }
                        if (isMyProfile && user != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_log_out)) },
                                onClick = { menuExpanded = false; onLogOut() }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun NotLoggedInContent(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_not_logged_in_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.profile_not_logged_in_text),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            TextButton(onClick = onSignIn) {
                Text(stringResource(R.string.action_log_in_to_kitsu))
            }
        }
    }
}
