package io.github.drumber.kitsune.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryUiModel
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.getStringResId
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingErrorContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingLoadingContent
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover
import kotlinx.coroutines.delay

private val libraryStatusFilters = listOf(
    LibraryStatus.Current,
    LibraryStatus.Planned,
    LibraryStatus.Completed,
    LibraryStatus.OnHold,
    LibraryStatus.Dropped
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: UiState,
    libraryEntries: LazyPagingItems<LibraryEntryUiModel>,
    offlineSyncCount: Int,
    isLoggedIn: Boolean,
    gridState: LazyGridState,
    snackbarHostState: SnackbarHostState,
    onSearch: (String) -> Unit,
    onKindSelected: (LibraryEntryKind) -> Unit,
    onStatusToggle: (LibraryStatus) -> Unit,
    onSyncClicked: () -> Unit,
    onDbRequestClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onEntryClicked: (LibraryEntryWithModification) -> Unit,
    onEntryLongClicked: (LibraryEntryWithModification) -> Unit,
    onEpisodeWatched: (LibraryEntryWithModification) -> Unit,
    onEpisodeUnwatched: (LibraryEntryWithModification) -> Unit,
    onRatingClicked: (LibraryEntryWithModification) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf(uiState.filter.searchQuery) }
    var searchActive by rememberSaveable { mutableStateOf(uiState.filter.searchQuery.isNotBlank()) }

    LaunchedEffect(searchQuery) {
        delay(300L)
        onSearch(searchQuery)
    }

    LaunchedEffect(uiState.filter.createTime) {
        if (gridState.firstVisibleItemIndex > 0) {
            gridState.animateScrollToItem(0)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            LibraryTopBar(
                filterState = uiState.filter,
                searchActive = searchActive,
                searchQuery = searchQuery,
                offlineSyncCount = offlineSyncCount,
                isLoggedIn = isLoggedIn,
                onSearchActiveChange = { searchActive = it; if (!it) { searchQuery = "" } },
                onSearchQueryChange = { searchQuery = it },
                onSyncClicked = onSyncClicked,
                onDbRequestClicked = onDbRequestClicked,
                onKindSelected = onKindSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.isLibraryUpdateOperationInProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (isLoggedIn) {
                LibraryFilterRow(
                    filterState = uiState.filter,
                    onStatusToggle = onStatusToggle
                )
                LibraryEntriesContent(
                    libraryEntries = libraryEntries,
                    gridState = gridState,
                    offlineSyncCount = offlineSyncCount,
                    onEntryClicked = onEntryClicked,
                    onEntryLongClicked = onEntryLongClicked,
                    onEpisodeWatched = onEpisodeWatched,
                    onEpisodeUnwatched = onEpisodeUnwatched,
                    onRatingClicked = onRatingClicked,
                    onSyncClicked = onSyncClicked
                )
            } else {
                LibraryNotLoggedIn(
                    modifier = Modifier.fillMaxSize(),
                    onLoginClicked = onLoginClicked
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
    filterState: FilterState,
    searchActive: Boolean,
    searchQuery: String,
    offlineSyncCount: Int,
    isLoggedIn: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSyncClicked: () -> Unit,
    onDbRequestClicked: () -> Unit,
    onKindSelected: (LibraryEntryKind) -> Unit
) {
    var showKindDialog by remember { mutableStateOf(false) }

    if (showKindDialog) {
        LibraryKindDialog(
            currentKind = filterState.kind,
            onKindSelected = { onKindSelected(it); showKindDialog = false },
            onDismiss = { showKindDialog = false }
        )
    }

    TopAppBar(
        title = {
            if (searchActive) {
                LibrarySearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClose = { onSearchActiveChange(false) }
                )
            } else {
                Text(stringResource(R.string.nav_library))
            }
        },
        actions = {
            if (!searchActive && isLoggedIn) {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(Icons.Default.Search, stringResource(R.string.hint_search))
                }
                if (offlineSyncCount > 0) {
                    BadgedBox(badge = { Badge { Text(offlineSyncCount.toString()) } }) {
                        IconButton(onClick = onSyncClicked) {
                            Icon(Icons.Default.Sync, stringResource(R.string.action_synchronize))
                        }
                    }
                }
                IconButton(onClick = onDbRequestClicked) {
                    Text(
                        text = "DB",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    )
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.hint_search)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Remove, stringResource(R.string.action_close))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LibraryFilterRow(
    filterState: FilterState,
    onStatusToggle: (LibraryStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnime = filterState.kind != LibraryEntryKind.Manga
    val selectedStatuses = filterState.libraryStatus

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        libraryStatusFilters.forEach { status ->
            FilterChip(
                selected = selectedStatuses.contains(status),
                onClick = { onStatusToggle(status) },
                label = { Text(stringResource(status.getStringResId(isAnime))) }
            )
        }
    }
}

@Composable
private fun LibraryEntriesContent(
    libraryEntries: LazyPagingItems<LibraryEntryUiModel>,
    gridState: LazyGridState,
    offlineSyncCount: Int,
    onEntryClicked: (LibraryEntryWithModification) -> Unit,
    onEntryLongClicked: (LibraryEntryWithModification) -> Unit,
    onEpisodeWatched: (LibraryEntryWithModification) -> Unit,
    onEpisodeUnwatched: (LibraryEntryWithModification) -> Unit,
    onRatingClicked: (LibraryEntryWithModification) -> Unit,
    onSyncClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshState = libraryEntries.loadState.refresh
    val appendState = libraryEntries.loadState.append
    val isRefreshing = refreshState is LoadState.Loading && libraryEntries.itemCount > 0

    when {
        refreshState is LoadState.Loading && libraryEntries.itemCount == 0 ->
            PagingLoadingContent(modifier = modifier.fillMaxSize())
        refreshState is LoadState.Error && libraryEntries.itemCount == 0 ->
            PagingErrorContent(modifier = modifier.fillMaxSize(), onRetry = { libraryEntries.retry() })
        else -> {
            KitsunePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    if (offlineSyncCount > 0) onSyncClicked()
                    libraryEntries.refresh()
                },
                modifier = modifier.fillMaxSize()
            ) {
                LibraryEntriesGrid(
                    libraryEntries = libraryEntries,
                    appendState = appendState,
                    gridState = gridState,
                    onEntryClicked = onEntryClicked,
                    onEntryLongClicked = onEntryLongClicked,
                    onEpisodeWatched = onEpisodeWatched,
                    onEpisodeUnwatched = onEpisodeUnwatched,
                    onRatingClicked = onRatingClicked
                )
            }
        }
    }
}

@Composable
private fun LibraryEntriesGrid(
    libraryEntries: LazyPagingItems<LibraryEntryUiModel>,
    appendState: LoadState,
    gridState: LazyGridState,
    onEntryClicked: (LibraryEntryWithModification) -> Unit,
    onEntryLongClicked: (LibraryEntryWithModification) -> Unit,
    onEpisodeWatched: (LibraryEntryWithModification) -> Unit,
    onEpisodeUnwatched: (LibraryEntryWithModification) -> Unit,
    onRatingClicked: (LibraryEntryWithModification) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 350.dp),
        state = gridState,
        contentPadding = PaddingValues(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(
            count = libraryEntries.itemCount,
            key = libraryEntries.itemKey { model ->
                when (model) {
                    is LibraryEntryUiModel.EntryModel -> "e_${model.entry.id}"
                    is LibraryEntryUiModel.StatusSeparatorModel -> "s_${model.status.name}"
                }
            },
            span = { index ->
                val model = libraryEntries.peek(index)
                if (model is LibraryEntryUiModel.StatusSeparatorModel) {
                    GridItemSpan(maxLineSpan)
                } else {
                    GridItemSpan(1)
                }
            }
        ) { index ->
            when (val model = libraryEntries[index]) {
                is LibraryEntryUiModel.EntryModel -> LibraryEntryCard(
                    entry = model.entry,
                    onClick = { onEntryClicked(model.entry) },
                    onLongClick = { onEntryLongClicked(model.entry) },
                    onEpisodeWatched = { onEpisodeWatched(model.entry) },
                    onEpisodeUnwatched = { onEpisodeUnwatched(model.entry) },
                    onRatingClicked = { onRatingClicked(model.entry) }
                )
                is LibraryEntryUiModel.StatusSeparatorModel ->
                    LibraryStatusSeparatorRow(model = model)
                null -> LibraryEntryCardPlaceholder()
            }
        }
        when (appendState) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                PagingErrorContent(
                    modifier = Modifier.fillMaxWidth(),
                    onRetry = { libraryEntries.retry() }
                )
            }
            else -> Unit
        }
        if (appendState.endOfPaginationReached && libraryEntries.itemCount == 0) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PagingEmptyContent()
            }
        }
    }
}

@Composable
private fun LibraryEntryCard(
    entry: LibraryEntryWithModification,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEpisodeWatched: () -> Unit,
    onEpisodeUnwatched: () -> Unit,
    onRatingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.height(150.dp)) {
            MediaCover(
                imageUrl = entry.media?.posterImageUrl,
                modifier = Modifier
                    .width(106.dp)
                    .fillMaxSize()
            )
            LibraryEntryCardDetails(
                entry = entry,
                onLongClick = onLongClick,
                onEpisodeWatched = onEpisodeWatched,
                onEpisodeUnwatched = onEpisodeUnwatched,
                onRatingClicked = onRatingClicked,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun LibraryEntryCardDetails(
    entry: LibraryEntryWithModification,
    onLongClick: () -> Unit,
    onEpisodeWatched: () -> Unit,
    onEpisodeUnwatched: () -> Unit,
    onRatingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entry.media?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (entry.isNotSynced) 2 else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRatingClicked) {
                    Icon(
                        imageVector = if (entry.hasRating) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = stringResource(R.string.hint_rating)
                    )
                }
            }
            val mediaYear = entry.media?.publishingYearText(context) ?: ""
            val mediaSubtype = entry.media?.subtypeFormatted ?: ""
            if (mediaYear.isNotEmpty() || mediaSubtype.isNotEmpty()) {
                Text(
                    text = "$mediaYear • $mediaSubtype",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.isNotSynced) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.library_not_synchronized),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        LibraryEntryProgressRow(
            entry = entry,
            onLongClick = onLongClick,
            onEpisodeWatched = onEpisodeWatched,
            onEpisodeUnwatched = onEpisodeUnwatched
        )
    }
}

@Suppress("UnusedParameter")
@Composable
private fun LibraryEntryProgressRow(
    entry: LibraryEntryWithModification,
    onLongClick: () -> Unit,
    onEpisodeWatched: () -> Unit,
    onEpisodeUnwatched: () -> Unit
) {
    val progressText = if (entry.hasStartedWatching) {
        "${entry.progress} / ${entry.episodeCountFormatted}"
    } else {
        stringResource(R.string.library_not_started)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = progressText,
            style = MaterialTheme.typography.bodySmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEpisodeUnwatched,
                enabled = entry.hasStartedWatching,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = onEpisodeWatched,
                enabled = entry.canWatchEpisode,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun LibraryEntryCardPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {}
}

@Composable
private fun LibraryStatusSeparatorRow(
    model: LibraryEntryUiModel.StatusSeparatorModel,
    modifier: Modifier = Modifier
) {
    val label = stringResource(model.status.getStringResId(!model.isMangaSelected))
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun LibraryNotLoggedIn(
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.library_not_logged_in_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.library_not_logged_in_text),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        androidx.compose.material3.Button(onClick = onLoginClicked) {
            Text(stringResource(R.string.action_log_in_to_kitsu))
        }
    }
}

@Composable
private fun LibraryKindDialog(
    currentKind: LibraryEntryKind,
    onKindSelected: (LibraryEntryKind) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        LibraryEntryKind.All to stringResource(R.string.library_kind_all),
        LibraryEntryKind.Anime to stringResource(R.string.anime),
        LibraryEntryKind.Manga to stringResource(R.string.manga)
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_media_type)) },
        text = {
            Column {
                options.forEach { (kind, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = currentKind == kind,
                            onClick = { onKindSelected(kind) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
