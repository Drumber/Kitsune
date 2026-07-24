package io.github.drumber.kitsune.ui.component.compose.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A pull-to-refresh container backed by Material 3's [PullToRefreshBox].
 *
 * Callers do not need to opt-in to [ExperimentalMaterial3Api] — the opt-in is scoped here.
 *
 * Typical usage with paging:
 * ```
 * val lazyItems = viewModel.dataFlow.collectAsLazyPagingItems()
 * KitsunePullToRefreshBox(
 *     isRefreshing = lazyItems.loadState.refresh is LoadState.Loading && lazyItems.itemCount > 0,
 *     onRefresh = { lazyItems.refresh() }
 * ) {
 *     PagingColumn(items = lazyItems) { item -> ItemRow(item) }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitsunePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        content = content
    )
}

// region Previews

@Preview(showBackground = true, name = "Not refreshing")
@Composable
private fun KitsunePullToRefreshBoxIdlePreview() {
    KitsuneTheme {
        KitsunePullToRefreshBox(
            isRefreshing = false,
            onRefresh = {}
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true, name = "Refreshing")
@Composable
private fun KitsunePullToRefreshBoxRefreshingPreview() {
    KitsuneTheme {
        KitsunePullToRefreshBox(
            isRefreshing = true,
            onRefresh = {}
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

// endregion
