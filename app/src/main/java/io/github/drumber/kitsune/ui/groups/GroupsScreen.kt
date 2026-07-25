package io.github.drumber.kitsune.ui.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn
import io.github.drumber.kitsune.ui.component.compose.media.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    groups: LazyPagingItems<Group>,
    categories: List<GroupCategory>,
    selectedCategoryId: String?,
    isLoggedIn: Boolean,
    isFollowingEnabled: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFollowingToggle: (Boolean) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onGroupClick: (Group) -> Unit,
    onNavigateUp: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                KitsuneTopAppBar(
                    title = { Text(stringResource(R.string.title_groups)) },
                    navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                    scrollBehavior = scrollBehavior
                )
                GroupsSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                GroupsFilterRow(
                    isLoggedIn = isLoggedIn,
                    isFollowingEnabled = isFollowingEnabled,
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onFollowingToggle = onFollowingToggle,
                    onCategorySelect = onCategorySelect
                )
            }
        }
    ) { innerPadding ->
        val isRefreshing = groups.loadState.refresh is LoadState.Loading &&
            groups.itemCount > 0

        KitsunePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { groups.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PagingColumn(
                items = groups,
                modifier = Modifier.fillMaxSize(),
                key = { it.id }
            ) { group ->
                GroupRow(group = group, onClick = { group?.let(onGroupClick) })
            }
        }
    }
}

@Composable
private fun GroupsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.groups_search_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun GroupsFilterRow(
    isLoggedIn: Boolean,
    isFollowingEnabled: Boolean,
    categories: List<GroupCategory>,
    selectedCategoryId: String?,
    onFollowingToggle: (Boolean) -> Unit,
    onCategorySelect: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoggedIn) {
            item {
                FilterChip(
                    selected = isFollowingEnabled,
                    onClick = { onFollowingToggle(!isFollowingEnabled) },
                    label = { Text(stringResource(R.string.groups_filter_following)) },
                    leadingIcon = {
                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelect(null) },
                label = { Text(stringResource(R.string.groups_category_all)) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        items(categories) { category ->
            val name = category.name ?: return@items
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelect(if (selectedCategoryId == category.id) null else category.id) },
                label = { Text(name) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun GroupRow(group: Group?, onClick: () -> Unit) {
    if (group == null) {
        GroupRowPlaceholder()
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(imageUrl = group.avatarUrl, size = 48.dp, contentDescription = group.name)
        Spacer(Modifier.width(12.dp))
        GroupRowContent(
            group = group,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GroupRowContent(group: Group, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = group.name.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val membersText = pluralStringResource(
            R.plurals.group_members_count,
            group.membersCount,
            group.membersCount
        )
        Text(
            text = membersText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val tagline = group.tagline?.takeUnless { it.isBlank() }
        if (tagline != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GroupRowPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(imageUrl = null, size = 48.dp)
        Spacer(Modifier.width(12.dp))
    }
}
