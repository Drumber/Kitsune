package io.github.drumber.kitsune.ui.search.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algolia.instantsearch.core.number.range.Range
import com.algolia.search.model.search.Facet
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.search.SearchViewModel.SearchClientStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacetScreen(
    modifier: Modifier = Modifier,
    clientStatus: SearchClientStatus,
    onRetrySearchClient: () -> Unit,
    filterCount: Int,
    onResetFilter: () -> Unit,
    onNavigateUp: () -> Unit,
    onCategoriesClick: () -> Unit,
    categoriesCount: Int,
    kindState: FacetListViewState,
    yearState: NumberRangeViewState,
    avgRatingState: NumberRangeViewState,
    seasonState: FacetListViewState,
    subtypeState: FacetListViewState,
    streamersState: FacetListViewState,
    ageRatingState: FacetListViewState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneTopAppBar(
                title = { Text(stringResource(R.string.title_filter)) },
                navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
                actions = {
                    if (filterCount > 0) {
                        IconButton(onClick = onResetFilter) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_reset_filter))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        when (clientStatus) {
            SearchClientStatus.NotInitialized -> FacetLoadingContent(modifier = Modifier.padding(innerPadding))
            SearchClientStatus.NotAvailable, SearchClientStatus.Error ->
                FacetErrorContent(onRetry = onRetrySearchClient, modifier = Modifier.padding(innerPadding))
            SearchClientStatus.Initialized -> FacetFiltersContent(
                modifier = Modifier.padding(innerPadding),
                kindState = kindState,
                yearState = yearState,
                avgRatingState = avgRatingState,
                seasonState = seasonState,
                subtypeState = subtypeState,
                streamersState = streamersState,
                ageRatingState = ageRatingState,
                onCategoriesClick = onCategoriesClick,
                categoriesCount = categoriesCount
            )
        }
    }
}

@Composable
private fun FacetLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { CircularProgressIndicator() }
}

@Composable
private fun FacetErrorContent(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.search_provider_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        androidx.compose.material3.FilledTonalButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun FacetFiltersContent(
    modifier: Modifier = Modifier,
    kindState: FacetListViewState,
    yearState: NumberRangeViewState,
    avgRatingState: NumberRangeViewState,
    seasonState: FacetListViewState,
    subtypeState: FacetListViewState,
    streamersState: FacetListViewState,
    ageRatingState: FacetListViewState,
    onCategoriesClick: () -> Unit,
    categoriesCount: Int
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
        val kindItems by kindState.items.collectAsStateWithLifecycle()
        FacetChipsSection(label = stringResource(R.string.filter_kind), items = kindItems, onSelect = kindState::select)
        val yearBounds by yearState.bounds.collectAsStateWithLifecycle()
        val yearRange by yearState.range.collectAsStateWithLifecycle()
        FacetRangeSection(
            label = stringResource(R.string.filter_year),
            bounds = yearBounds,
            range = yearRange,
            onRangeChanged = yearState::changeRange,
            formatValue = { min, max -> if (max >= (yearBounds?.max ?: max)) "$min - ∞" else "$min - $max" }
        )
        val avgRatingBounds by avgRatingState.bounds.collectAsStateWithLifecycle()
        val avgRatingRange by avgRatingState.range.collectAsStateWithLifecycle()
        FacetRangeSection(
            label = stringResource(R.string.filter_avg_rating),
            bounds = avgRatingBounds,
            range = avgRatingRange,
            onRangeChanged = avgRatingState::changeRange,
            formatValue = { min, max -> "$min% - $max%" }
        )
        CategoriesSection(onCategoriesClick = onCategoriesClick, categoriesCount = categoriesCount)
        val seasonItems by seasonState.items.collectAsStateWithLifecycle()
        FacetChipsSection(
            label = stringResource(R.string.filter_season),
            items = seasonItems,
            onSelect = seasonState::select
        )
        val subtypeItems by subtypeState.items.collectAsStateWithLifecycle()
        FacetChipsSection(
            label = stringResource(R.string.filter_subtype),
            items = subtypeItems,
            onSelect = subtypeState::select
        )
        val streamersItems by streamersState.items.collectAsStateWithLifecycle()
        FacetChipsSection(
            label = stringResource(R.string.filter_streamers),
            items = streamersItems,
            onSelect = streamersState::select
        )
        val ageRatingItems by ageRatingState.items.collectAsStateWithLifecycle()
        FacetChipsSection(
            label = stringResource(R.string.filter_age_rating),
            items = ageRatingItems,
            onSelect = ageRatingState::select
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FacetChipsSection(
    label: String,
    items: List<Pair<Facet, Boolean>>,
    onSelect: (Facet) -> Unit
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.headlineSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (facet, selected) ->
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(facet) },
                    label = { Text("${facet.value.replaceFirstChar(Char::titlecase)} (${facet.count})") }
                )
            }
        }
    }
}

@Composable
private fun FacetRangeSection(
    label: String,
    bounds: Range<Int>?,
    range: Range<Int>?,
    onRangeChanged: (Range<Int>?) -> Unit,
    formatValue: (min: Int, max: Int) -> String
) {
    if (bounds == null) return
    val effectiveMin = range?.min?.toFloat() ?: bounds.min.toFloat()
    val effectiveMax = range?.max?.toFloat() ?: bounds.max.toFloat()
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatValue(effectiveMin.toInt(), effectiveMax.toInt()),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        RangeSlider(
            value = effectiveMin..effectiveMax,
            onValueChange = { sliderRange ->
                val newMin = sliderRange.start.toInt()
                val newMax = sliderRange.endInclusive.toInt()
                if (newMin == bounds.min && newMax == bounds.max) {
                    onRangeChanged(null)
                } else {
                    onRangeChanged(Range(newMin..newMax))
                }
            },
            valueRange = bounds.min.toFloat()..bounds.max.toFloat(),
            steps = 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoriesSection(onCategoriesClick: () -> Unit, categoriesCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = stringResource(R.string.title_categories), style = MaterialTheme.typography.headlineSmall)
        Surface(
            onClick = onCategoriesClick,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_select_categories),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (categoriesCount > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = categoriesCount.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
