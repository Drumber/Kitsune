package io.github.drumber.kitsune.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.algolia.SearchType
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.UserSearchResult
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingErrorContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingLoadingContent
import io.github.drumber.kitsune.ui.component.compose.media.Avatar
import io.github.drumber.kitsune.ui.component.compose.media.MediaItemCard
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    query: String,
    isSearchFocused: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    clientStatus: SearchClientStatus,
    onRetrySearchClient: () -> Unit,
    filterCount: Int,
    onFilterClick: () -> Unit,
    onFilterLongClick: () -> Unit,
    gridState: LazyGridState,
    columnState: LazyListState,
    searchItems: LazyPagingItems<Any>,
    onMediaClick: (Media) -> Unit,
    onUserClick: (UserSearchResult) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchTopBar(
                query = query,
                isSearchFocused = isSearchFocused,
                onQueryChange = onQueryChange,
                onSearchFocusChange = onSearchFocusChange,
                searchType = searchType,
                onSearchTypeChange = onSearchTypeChange,
                filterCount = filterCount,
                onFilterClick = onFilterClick,
                onFilterLongClick = onFilterLongClick,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (clientStatus != SearchClientStatus.Initialized) {
                SearchClientStatusContent(
                    clientStatus = clientStatus,
                    onRetry = onRetrySearchClient,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                SearchResultsContent(
                    searchType = searchType,
                    items = searchItems,
                    gridState = gridState,
                    columnState = columnState,
                    onMediaClick = onMediaClick,
                    onUserClick = onUserClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnusedParameter")
@Composable
private fun SearchTopBar(
    query: String,
    isSearchFocused: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    filterCount: Int,
    onFilterClick: () -> Unit,
    onFilterLongClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 0.dp) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            SearchInputCard(
                query = query,
                isSearchFocused = isSearchFocused,
                onQueryChange = onQueryChange,
                onSearchFocusChange = onSearchFocusChange,
                searchType = searchType,
                filterCount = filterCount,
                onFilterClick = onFilterClick,
                onFilterLongClick = onFilterLongClick
            )
            Spacer(modifier = Modifier.height(4.dp))
            SearchTypeToggle(
                searchType = searchType,
                onSearchTypeChange = onSearchTypeChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SearchInputCard(
    query: String,
    isSearchFocused: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    searchType: SearchType,
    filterCount: Int,
    onFilterClick: () -> Unit,
    onFilterLongClick: () -> Unit
) {
    OutlinedCard(shape = MaterialTheme.shapes.extraLarge) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { onSearchFocusChange(!isSearchFocused) }) {
                Icon(
                    imageVector = if (isSearchFocused) {
                        Icons.AutoMirrored.Filled.ArrowBack
                    } else {
                        Icons.Filled.Search
                    },
                    contentDescription = stringResource(R.string.hint_search)
                )
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag(KitsuneTestTags.SearchInput),
                placeholder = { Text(stringResource(R.string.hint_search)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
            if (searchType != SearchType.Users) {
                FilterIconButton(
                    filterCount = filterCount,
                    onClick = onFilterClick,
                    onLongClick = onFilterLongClick
                )
            }
        }
    }
}

@Composable
@Suppress("UnusedParameter")
private fun FilterIconButton(filterCount: Int, onClick: () -> Unit, onLongClick: () -> Unit) {
    BadgedBox(
        badge = { if (filterCount > 0) Badge { Text(filterCount.toString()) } },
        modifier = Modifier.padding(end = 4.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = stringResource(R.string.title_filter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTypeToggle(
    searchType: SearchType,
    onSearchTypeChange: (SearchType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = searchType != SearchType.Users,
            onClick = { onSearchTypeChange(SearchType.Media) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) { Text(stringResource(R.string.search_type_media)) }
        SegmentedButton(
            selected = searchType == SearchType.Users,
            onClick = { onSearchTypeChange(SearchType.Users) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) { Text(stringResource(R.string.search_type_users)) }
    }
}

@Composable
private fun SearchResultsContent(
    searchType: SearchType,
    items: LazyPagingItems<Any>,
    gridState: LazyGridState,
    columnState: LazyListState,
    onMediaClick: (Media) -> Unit,
    onUserClick: (UserSearchResult) -> Unit
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append
    when {
        refresh is LoadState.Loading && items.itemCount == 0 ->
            PagingLoadingContent(modifier = Modifier.fillMaxSize())
        refresh is LoadState.Error && items.itemCount == 0 ->
            PagingErrorContent(modifier = Modifier.fillMaxSize(), onRetry = { items.retry() })
        refresh is LoadState.NotLoading &&
            append.endOfPaginationReached &&
            items.itemCount == 0 -> PagingEmptyContent()
        searchType == SearchType.Users ->
            SearchUsersList(items = items, state = columnState, onUserClick = onUserClick)
        else ->
            SearchMediaGrid(items = items, state = gridState, onMediaClick = onMediaClick)
    }
}

@Composable
private fun SearchMediaGrid(
    items: LazyPagingItems<Any>,
    state: LazyGridState,
    onMediaClick: (Media) -> Unit
) {
    val append = items.loadState.append
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        state = state,
        modifier = Modifier.testTag(KitsuneTestTags.SearchResults),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey(),
            contentType = items.itemContentType()
        ) { index ->
            (items[index] as? Media)?.let { media ->
                MediaItemCard(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f),
                    imageUrl = media.posterImageUrl,
                    title = media.title,
                    subtypeLabel = media.subtypeFormatted.ifBlank { null },
                    onClick = { onMediaClick(media) }
                )
            }
        }
        when (append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                SearchAppendError(onRetry = { items.retry() })
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun SearchUsersList(
    items: LazyPagingItems<Any>,
    state: LazyListState,
    onUserClick: (UserSearchResult) -> Unit
) {
    val append = items.loadState.append
    LazyColumn(state = state, contentPadding = PaddingValues(vertical = 8.dp)) {
        items(
            count = items.itemCount,
            key = items.itemKey(),
            contentType = items.itemContentType()
        ) { index ->
            (items[index] as? UserSearchResult)?.let { user ->
                UserResultRow(user = user, onClick = { onUserClick(user) })
            }
        }
        when (append) {
            is LoadState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is LoadState.Error -> item { SearchAppendError(onRetry = { items.retry() }) }
            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun SearchAppendError(onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_resource_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun UserResultRow(user: UserSearchResult, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Avatar(imageUrl = user.avatar?.smallOrHigher(), size = 48.dp, contentDescription = user.name)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = user.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                if (!user.title.isNullOrBlank()) {
                    Text(
                        text = user.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchClientStatusContent(
    clientStatus: SearchClientStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (clientStatus) {
            SearchClientStatus.NotInitialized -> CircularProgressIndicator()
            SearchClientStatus.NotAvailable -> SearchClientErrorContent(
                message = stringResource(R.string.search_provider_not_available),
                onRetry = onRetry
            )
            SearchClientStatus.Error -> SearchClientErrorContent(
                message = stringResource(R.string.search_provider_error),
                onRetry = onRetry
            )
            SearchClientStatus.Initialized -> Unit
        }
    }
}

@Composable
private fun SearchClientErrorContent(message: String, onRetry: () -> Unit) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    FilledTonalButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
}
