package io.github.drumber.kitsune.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.list.KitsunePullToRefreshBox
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    animeExploreScreenContent: @Composable () -> Unit,
    mangaExploreScreenContent: @Composable () -> Unit,
    isRefreshing: Boolean,
    onRefresh: (isAnime: Boolean) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { 2 }
) {
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf(stringResource(R.string.anime), stringResource(R.string.manga))

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                SearchBarButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
        }
    ) { paddingValues ->
        KitsunePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh(pagerState.currentPage == 0) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (page) {
                        0 -> animeExploreScreenContent()
                        1 -> mangaExploreScreenContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.hint_search_media),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
