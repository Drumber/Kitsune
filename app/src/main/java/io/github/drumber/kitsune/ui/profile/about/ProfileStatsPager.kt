package io.github.drumber.kitsune.ui.profile.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsKind
import io.github.drumber.kitsune.ui.component.compose.chart.DonutChart
import io.github.drumber.kitsune.ui.component.compose.chart.DonutSlice
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.util.TimeUtil
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Maximum number of category slices shown, carried over from `PieChartStyle.STATS_MAX_ELEMENTS`. */
private const val STATS_MAX_ELEMENTS = 7

private const val CHART_HEIGHT_DP = 220

/**
 * Category palette, previously `R.array.stats_chart_colors`. Kept in Kotlin so the chart carries
 * no Android resource dependency.
 */
private val StatsChartColors = listOf(
    Color(0xFFFEB700),
    Color(0xFFFF9300),
    Color(0xFFFF3281),
    Color(0xFFBC6EDA),
    Color(0xFF00BBED),
    Color(0xFF545C97),
    Color(0xFFEA6200)
)

private const val PAGE_ANIME = 0
private const val PAGE_MANGA = 1

/**
 * Anime/manga profile statistics: a tabbed pager with a category-breakdown donut chart and the
 * amount-consumed summary.
 *
 * Replaces `ProfileStatsSection` + `ProfileStatsAdapter` (ViewPager2 + TabLayout + MPAndroidChart).
 */
@Composable
fun ProfileStatsPager(
    stats: List<UserStats>?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == PAGE_ANIME,
                onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_ANIME) } },
                text = { Text(stringResource(R.string.profile_anime_stats)) }
            )
            Tab(
                selected = pagerState.currentPage == PAGE_MANGA,
                onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_MANGA) } },
                text = { Text(stringResource(R.string.profile_manga_stats)) }
            )
        }

        HorizontalPager(state = pagerState) { page ->
            val isAnime = page == PAGE_ANIME
            ProfileStatsPage(
                title = stringResource(
                    if (isAnime) R.string.profile_anime_stats else R.string.profile_manga_stats
                ),
                isAnime = isAnime,
                categoryBreakdown = stats.findStatsData(
                    if (isAnime) UserStatsKind.AnimeCategoryBreakdown
                    else UserStatsKind.MangaCategoryBreakdown
                ),
                amountConsumed = stats.findStatsData(
                    if (isAnime) UserStatsKind.AnimeAmountConsumed
                    else UserStatsKind.MangaAmountConsumed
                ),
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun ProfileStatsPage(
    title: String,
    isAnime: Boolean,
    categoryBreakdown: UserStatsData.CategoryBreakdownData?,
    amountConsumed: UserStatsData.AmountConsumedData?,
    isLoading: Boolean
) {
    val slices = remember(categoryBreakdown) { categoryBreakdown.toSlices() }
    // Mirrors the old adapter, which pre-highlighted the largest slice.
    var selectedIndex by rememberSaveable(slices.size) { mutableStateOf(if (slices.isEmpty()) null else 0) }

    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                slices.isEmpty() -> Text(
                    text = stringResource(R.string.no_data_available),
                    style = MaterialTheme.typography.titleMedium
                )

                else -> DonutChart(
                    slices = slices,
                    selectedIndex = selectedIndex,
                    onSliceSelected = { selectedIndex = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val selected = selectedIndex?.let(slices::getOrNull)
                    if (selected == null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        SliceTooltip(selected)
                    }
                }
            }
        }

        if (!isLoading && amountConsumed != null) {
            Spacer(Modifier.height(16.dp))
            AmountConsumedCard(isAnime = isAnime, data = amountConsumed)
        }
    }
}

@Composable
private fun SliceTooltip(slice: DonutSlice) {
    Card(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(slice.color)
            )
            Text(
                text = slice.label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.profile_stats_percent, slice.value.roundToInt()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AmountConsumedCard(isAnime: Boolean, data: UserStatsData.AmountConsumedData) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val headline = if (isAnime) {
                data.time?.let {
                    stringResource(
                        R.string.profile_stats_anime_watch_time,
                        TimeUtil.roundTime(it, context, 1)
                    )
                }
            } else {
                data.units?.let { stringResource(R.string.profile_stats_manga_chapters_read, it) }
            }
            if (headline != null) {
                Text(text = headline, style = MaterialTheme.typography.titleSmall)
            }

            if (isAnime) {
                data.time?.takeIf { it > 0 }?.let { time ->
                    Text(
                        text = stringResource(
                            R.string.profile_stats_time_spent_total,
                            TimeUtil.timeToHumanReadableFormat(time, context)
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            val completed = data.completed
            val percentile = if (isAnime) data.percentiles?.time else data.percentiles?.units
            if (completed != null && percentile != null) {
                Text(
                    text = AnnotatedString.fromHtml(
                        stringResource(
                            R.string.profile_stats_completed,
                            completed,
                            percentile.times(100).roundToInt().coerceIn(0..99)
                        )
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun UserStatsData.CategoryBreakdownData?.toSlices(): List<DonutSlice> {
    val total = this?.total ?: return emptyList()
    val categories = this.categories ?: return emptyList()
    if (total <= 0) return emptyList()

    return categories.toList()
        .filter { it.second != 0 }
        .sortedByDescending { it.second }
        .take(STATS_MAX_ELEMENTS)
        .mapIndexed { index, (category, value) ->
            DonutSlice(
                label = category,
                value = value.toFloat() / total * 100f,
                color = StatsChartColors[index % StatsChartColors.size]
            )
        }
}

private inline fun <reified T> List<UserStats>?.findStatsData(kind: UserStatsKind): T? {
    return this?.find { it.kind == kind }?.statsData as? T
}

@Preview(showBackground = true)
@Composable
private fun ProfileStatsPagerPreview() {
    KitsuneTheme {
        ProfileStatsPager(
            stats = null,
            isLoading = false
        )
    }
}
