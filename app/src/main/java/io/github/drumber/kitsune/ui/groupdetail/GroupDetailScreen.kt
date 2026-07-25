package io.github.drumber.kitsune.ui.groupdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import kotlinx.coroutines.flow.Flow

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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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
            KitsuneTopAppBar(
                title = { Text(group?.name.orEmpty()) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
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
                membershipState = membershipState,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onJoinLeave = onJoinLeave,
                onOpenCover = onOpenCover,
                feedContent = feedContent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
@Suppress("UnusedParameter")
private fun GroupDetailContent(
    group: Group?,
    membershipState: GroupDetailViewModel.MembershipState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onJoinLeave: () -> Unit,
    onOpenCover: () -> Unit,
    feedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        GroupDetailHeader(group = group, membershipState = membershipState, onJoinLeave = onJoinLeave)
        GroupDetailTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
        when (selectedTab) {
            TAB_ABOUT -> GroupAboutTab(group = group, modifier = Modifier.fillMaxSize())
            TAB_POSTS -> Box(modifier = Modifier.fillMaxSize()) { feedContent() }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GroupDetailHeader(
    group: Group?,
    membershipState: GroupDetailViewModel.MembershipState,
    onJoinLeave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            GlideImage(
                model = group?.coverImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) { it.placeholder(R.drawable.cover_placeholder) }
            Avatar(
                imageUrl = group?.avatarUrl,
                size = 64.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
        GroupDetailInfo(group = group, membershipState = membershipState, onJoinLeave = onJoinLeave)
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
