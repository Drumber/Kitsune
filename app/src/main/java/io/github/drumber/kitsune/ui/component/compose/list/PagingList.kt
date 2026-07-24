package io.github.drumber.kitsune.ui.component.compose.list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A vertically-scrolling paged list that handles all [LoadState] cases internally:
 * - Initial refresh loading: full-screen [PagingLoadingContent]
 * - Refresh error (no cached items): full-screen [PagingErrorContent] with retry
 * - Genuinely empty (end-of-pagination reached, 0 items): [emptyContent]
 * - Normal display: [LazyColumn] with append-loading / append-error footers
 *
 * Pull-to-refresh (when items > 0 and refresh is loading) is intentionally NOT shown here;
 * wrap this composable with [KitsunePullToRefreshBox] to get that indicator.
 */
@Composable
fun <T : Any> PagingColumn(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    key: ((item: T) -> Any)? = null,
    contentType: ((item: T) -> Any?)? = null,
    emptyContent: @Composable () -> Unit = { PagingEmptyContent() },
    itemContent: @Composable LazyItemScope.(item: T?) -> Unit
) {
    val refreshState = items.loadState.refresh
    val appendState = items.loadState.append

    when {
        refreshState is LoadState.Loading && items.itemCount == 0 ->
            PagingLoadingContent(modifier = modifier.fillMaxSize())

        refreshState is LoadState.Error && items.itemCount == 0 ->
            PagingErrorContent(modifier = modifier.fillMaxSize(), onRetry = { items.retry() })

        refreshState is LoadState.NotLoading &&
                appendState.endOfPaginationReached &&
                items.itemCount == 0 ->
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                emptyContent()
            }

        else -> LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement
        ) {
            items(
                count = items.itemCount,
                key = items.itemKey(key),
                contentType = items.itemContentType(contentType)
            ) { index ->
                itemContent(items[index])
            }
            when (appendState) {
                is LoadState.Loading -> item { PagingAppendLoadingItem() }
                is LoadState.Error -> item { PagingAppendErrorItem(onRetry = { items.retry() }) }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

/**
 * A paged grid that handles all [LoadState] cases internally.
 * Append-loading and append-error footers span the full row width.
 *
 * @see PagingColumn for full load-state documentation.
 */
@Composable
fun <T : Any> PagingGrid(
    items: LazyPagingItems<T>,
    columns: GridCells,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    key: ((item: T) -> Any)? = null,
    contentType: ((item: T) -> Any?)? = null,
    emptyContent: @Composable () -> Unit = { PagingEmptyContent() },
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit
) {
    val refreshState = items.loadState.refresh
    val appendState = items.loadState.append

    when {
        refreshState is LoadState.Loading && items.itemCount == 0 ->
            PagingLoadingContent(modifier = modifier.fillMaxSize())

        refreshState is LoadState.Error && items.itemCount == 0 ->
            PagingErrorContent(modifier = modifier.fillMaxSize(), onRetry = { items.retry() })

        refreshState is LoadState.NotLoading &&
                appendState.endOfPaginationReached &&
                items.itemCount == 0 ->
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                emptyContent()
            }

        else -> LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement
        ) {
            items(
                count = items.itemCount,
                key = items.itemKey(key),
                contentType = items.itemContentType(contentType)
            ) { index ->
                itemContent(items[index])
            }
            when (appendState) {
                is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    PagingAppendLoadingItem()
                }
                is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                    PagingAppendErrorItem(onRetry = { items.retry() })
                }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}

/** Full-screen loading spinner shown during the initial page fetch. */
@Composable
fun PagingLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Full-screen empty state shown when paging has completed with zero items. */
@Composable
fun PagingEmptyContent(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.error_nothing_found)
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Full-screen error state with retry button, shown when the initial fetch fails. */
@Composable
fun PagingErrorContent(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.error_resource_loading),
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun PagingAppendLoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun PagingAppendErrorItem(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error_resource_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
private fun PagingLoadingContentPreview() {
    KitsuneTheme {
        PagingLoadingContent(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun PagingEmptyContentPreview() {
    KitsuneTheme {
        PagingEmptyContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun PagingErrorContentPreview() {
    KitsuneTheme {
        PagingErrorContent(onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PagingAppendLoadingItemPreview() {
    KitsuneTheme {
        PagingAppendLoadingItem()
    }
}

@Preview(showBackground = true)
@Composable
private fun PagingAppendErrorItemPreview() {
    KitsuneTheme {
        PagingAppendErrorItem(onRetry = {})
    }
}

// endregion
