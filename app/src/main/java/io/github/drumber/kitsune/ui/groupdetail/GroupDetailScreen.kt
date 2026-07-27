package io.github.drumber.kitsune.ui.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.util.extensions.getColor
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

private const val TAB_ABOUT = 0
private const val TAB_POSTS = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    modifier: Modifier = Modifier,
    group: Group?,
    isLoading: Boolean,
    membershipState: GroupDetailViewModel.MembershipState,
    events: Flow<GroupDetailViewModel.Event>,
    isMemberDefault: Boolean,
    onNavigateUp: () -> Unit,
    onJoinLeave: () -> Unit,
    onOpenCover: () -> Unit,
    onNavigateToCreatePost: () -> Unit,
    feedContent: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val loginRequiredMsg = stringResource(R.string.group_login_required)
    val joinFailedMsg = stringResource(R.string.group_join_failed)
    val leaveFailedMsg = stringResource(R.string.group_leave_failed)

    LaunchedEffect(Unit) {
        events.collect { event ->
            val msg = when (event) {
                GroupDetailViewModel.Event.LoginRequired -> loginRequiredMsg
                GroupDetailViewModel.Event.JoinFailed -> joinFailedMsg
                GroupDetailViewModel.Event.LeaveFailed -> leaveFailedMsg
            }
            snackbarHostState.showSnackbar(msg)
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_ABOUT) }
    var hasAutoSelectedPostsTab by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isMemberDefault) {
        if (isMemberDefault && !hasAutoSelectedPostsTab) {
            hasAutoSelectedPostsTab = true
            selectedTab = TAB_POSTS
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                GroupDetailTopBar(
                    group = group,
                    collapsedFraction = scrollBehavior.state.collapsedFraction,
                    scrollBehavior = scrollBehavior,
                    onNavigateUp = onNavigateUp,
                    onOpenCover = onOpenCover
                )
                GroupDetailHeader(
                    group = group,
                    membershipState = membershipState,
                    onJoinLeave = onJoinLeave,
                    modifier = Modifier.collapseWith(scrollBehavior.state.collapsedFraction)
                )
                GroupDetailTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        },
        floatingActionButton = {
            if (selectedTab == TAB_POSTS && membershipState.isVisible) {
                FloatingActionButton(onClick = onNavigateToCreatePost) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.title_create_post))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            GroupDetailContent(
                group = group,
                selectedTab = selectedTab,
                feedContent = feedContent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun GroupDetailContent(
    group: Group?,
    selectedTab: Int,
    feedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // The feed container remains in the tree to preserve its paging and scroll state.
    Box(modifier = modifier) {
        if (selectedTab == TAB_ABOUT) {
            GroupAboutTab(group = group, modifier = Modifier.fillMaxSize())
        }
        Box(
            modifier = if (selectedTab == TAB_POSTS)
                Modifier.fillMaxSize()
            else
                Modifier.requiredSize(0.dp)
        ) {
            feedContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailTopBar(
    group: Group?,
    collapsedFraction: Float,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateUp: () -> Unit,
    onOpenCover: () -> Unit
) {
    val context = LocalContext.current
    val coverPlaceholder = Brush.verticalGradient(
        colors = listOf(
            Color(context.theme.getColor(R.attr.colorPlaceholderGradientStart)),
            Color(context.theme.getColor(R.attr.colorPlaceholderGradientEnd))
        )
    )

    Box(modifier = Modifier.background(coverPlaceholder)) {
        AsyncImage(
            model = group?.coverImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 1f - collapsedFraction }
                .clickable(
                    enabled = group?.coverImageUrl != null,
                    onClick = onOpenCover
                )
        )
        LargeTopAppBar(
            title = {
                Text(
                    text = group?.name.orEmpty(),
                    modifier = Modifier.graphicsLayer { alpha = collapsedFraction }
                )
            },
            navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun GroupDetailHeader(
    group: Group?,
    membershipState: GroupDetailViewModel.MembershipState,
    onJoinLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Avatar(
            imageUrl = group?.avatarUrl,
            size = 64.dp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 12.dp)
        )
        GroupDetailInfo(group = group, membershipState = membershipState, onJoinLeave = onJoinLeave)
    }
}

private fun Modifier.collapseWith(collapsedFraction: Float): Modifier {
    val visibleFraction = 1f - collapsedFraction.coerceIn(0f, 1f)
    return this
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val visibleHeight = (placeable.height * visibleFraction).roundToInt()
            layout(placeable.width, visibleHeight) {
                placeable.placeRelative(0, 0)
            }
        }
        .graphicsLayer {
            alpha = visibleFraction
            clip = true
        }
}

@Composable
private fun GroupDetailInfo(
    group: Group?,
    membershipState: GroupDetailViewModel.MembershipState,
    onJoinLeave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = group?.name.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        val tagline = group?.tagline?.takeUnless { it.isBlank() }
        if (tagline != null) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val membersCount = group?.membersCount ?: 0
        Text(
            text = pluralStringResource(R.plurals.group_members_count, membersCount, membersCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        val categoryName = group?.categoryName?.takeUnless { it.isBlank() }
        if (categoryName != null) {
            AssistChip(onClick = {}, label = { Text(categoryName) })
        }
        if (membershipState.isVisible) {
            Spacer(Modifier.height(8.dp))
            MembershipButton(state = membershipState, onJoinLeave = onJoinLeave)
        }
    }
}

@Composable
private fun MembershipButton(
    state: GroupDetailViewModel.MembershipState,
    onJoinLeave: () -> Unit
) {
    if (state.isMember) {
        OutlinedButton(onClick = onJoinLeave, enabled = !state.isLoading) {
            Text(stringResource(R.string.group_action_leave))
        }
    } else {
        Button(onClick = onJoinLeave, enabled = !state.isLoading) {
            Text(stringResource(R.string.group_action_join))
        }
    }
}

@Composable
private fun GroupDetailTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == TAB_ABOUT,
            onClick = { onTabSelected(TAB_ABOUT) },
            text = { Text(stringResource(R.string.group_tab_about)) }
        )
        Tab(
            selected = selectedTab == TAB_POSTS,
            onClick = { onTabSelected(TAB_POSTS) },
            text = { Text(stringResource(R.string.group_tab_posts)) }
        )
    }
}

@Composable
private fun GroupAboutTab(group: Group?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val about = group?.about?.takeUnless { it.isBlank() }
        if (about != null) {
            GroupSection(title = stringResource(R.string.group_section_about), content = about)
            Spacer(Modifier.height(16.dp))
        }
        val rules = group?.rules?.takeUnless { it.isBlank() }
        if (rules != null) {
            GroupSection(title = stringResource(R.string.group_section_rules), content = rules)
        }
    }
}

@Composable
private fun GroupSection(title: String, content: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Text(text = content, style = MaterialTheme.typography.bodyMedium)
}
