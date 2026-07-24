package io.github.drumber.kitsune.ui.medialist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import io.github.drumber.kitsune.ui.component.compose.list.PagingEmptyContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingErrorContent
import io.github.drumber.kitsune.ui.component.compose.list.PagingLoadingContent
import io.github.drumber.kitsune.ui.component.compose.media.MediaItemCard
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import kotlinx.coroutines.flow.flowOf

/**
 * Displays a responsive paged grid of media items with a collapsing top bar.
 *
 * Grid column count and item aspect ratio are driven by caller-supplied [columns] and
 * [itemAspectRatio], which map directly to the [KitsunePref.mediaItemSize] preference —
 * keeping sizing logic in the Fragment rather than leaking preferences into this composable.
 *
 * @param columns        [GridCells.Adaptive] min size = item width + 2 × margin (5dp each side).
 * @param itemAspectRatio Width-to-height ratio matching the selected [MediaItemSize] preset.
 * @param gridState      Hoisted [LazyGridState]; the Fragment holds a reference for scroll-to-top.
 * @param topAppBarState Hoisted [TopAppBarState] so the Fragment can re-expand the collapsed
 *                       toolbar on bottom-nav reselect, matching `AppBarLayout.setExpanded(true)`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    modifier: Modifier = Modifier,
    title: String,
    items: LazyPagingItems<Media>,
    columns: GridCells,
    itemAspectRatio: Float,
    gridState: LazyGridState = rememberLazyGridState(),
    topAppBarState: TopAppBarState = rememberTopAppBarState(),
    onNavigateUp: () -> Unit,
    onMediaClick: (Media) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        MediaListGridContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            items = items,
            columns = columns,
            itemAspectRatio = itemAspectRatio,
            gridState = gridState,
            onMediaClick = onMediaClick
        )
    }
}

@Composable
private fun MediaListGridContent(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<Media>,
    columns: GridCells,
    itemAspectRatio: Float,
    gridState: LazyGridState,
    onMediaClick: (Media) -> Unit
) {
    val refreshState = items.loadState.refresh
    val appendState = items.loadState.append

    when {
        refreshState is LoadState.Loading && items.itemCount == 0 ->
            PagingLoadingContent(modifier = modifier)

        refreshState is LoadState.Error && items.itemCount == 0 ->
            PagingErrorContent(modifier = modifier, onRetry = { items.retry() })

        refreshState is LoadState.NotLoading &&
            appendState.endOfPaginationReached &&
            items.itemCount == 0 ->
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                PagingEmptyContent()
            }

        else -> {
            val isRefreshing = refreshState is LoadState.Loading && items.itemCount > 0
            KitsunePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { items.refresh() },
                modifier = modifier
            ) {
                LazyVerticalGrid(
                    columns = columns,
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalArrangement = Arrangement.Start
                ) {
                    items(
                        count = items.itemCount,
                        key = items.itemKey { it.id },
                        contentType = items.itemContentType { it.mediaType }
                    ) { index ->
                        val media = items[index]
                        MediaItemCard(
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth()
                                .aspectRatio(itemAspectRatio),
                            imageUrl = media?.posterImageUrl,
                            title = media?.title,
                            subtypeLabel = media?.subtypeFormatted,
                            onClick = { media?.let(onMediaClick) }
                        )
                    }
                    when (appendState) {
                        is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                            MediaListAppendLoading()
                        }
                        is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                            MediaListAppendError(onRetry = { items.retry() })
                        }
                        is LoadState.NotLoading -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaListAppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun MediaListAppendError(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "MediaList — empty")
@Composable
private fun MediaListEmptyPreview() {
    KitsuneTheme {
        val items = flowOf(PagingData.empty<Media>()).collectAsLazyPagingItems()
        MediaListScreen(
            title = "Trending Anime",
            items = items,
            columns = GridCells.Adaptive(151.dp),
            itemAspectRatio = 141f / 200f,
            onNavigateUp = {},
            onMediaClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "MediaList — with items")
@Composable
private fun MediaListWithItemsPreview() {
    KitsuneTheme {
        val items = flowOf(
            PagingData.from(
                listOf(
                    previewAnime("1", "Cowboy Bebop"),
                    previewAnime("2", "Attack on Titan"),
                    previewAnime("3", "Fullmetal Alchemist: Brotherhood"),
                    previewAnime("4", "Steins;Gate"),
                    previewAnime("5", "One Piece"),
                    previewAnime("6", "Naruto Shippuden")
                )
            )
        ).collectAsLazyPagingItems()
        MediaListScreen(
            title = "Trending Anime",
            items = items,
            columns = GridCells.Adaptive(151.dp),
            itemAspectRatio = 141f / 200f,
            onNavigateUp = {},
            onMediaClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "MediaList — loading")
@Composable
private fun MediaListLoadingPreview() {
    KitsuneTheme {
        PagingLoadingContent(modifier = Modifier.fillMaxSize())
    }
}

// endregion

private fun previewAnime(id: String, title: String): Media = Anime(
    id = id,
    slug = null,
    description = null,
    titles = null,
    canonicalTitle = title,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = null,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = null,
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = null,
    coverImage = null,
    totalLength = null,
    episodeCount = null,
    episodeLength = null,
    youtubeVideoId = null,
    subtype = null,
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)
