package io.github.drumber.kitsune.ui.profile.about

import androidx.annotation.StringRes
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsKind
import io.github.drumber.kitsune.ui.component.chart.PieChartStyle
import io.github.drumber.kitsune.util.extensions.recyclerView

/**
 * Encapsulates the profile statistics view pager (anime/manga category breakdown charts and
 * amount-consumed data). Used by both [io.github.drumber.kitsune.ui.profile.MyProfileFragment] and [io.github.drumber.kitsune.ui.profile.UserProfileFragment].
 */
class ProfileStatsSection(
    private val viewPager: ViewPager2,
    private val tabLayout: TabLayout
) {

    private val context get() = viewPager.context

    private lateinit var adapter: ProfileStatsAdapter

    fun init(isInitialCreation: Boolean) {
        val tabItems = listOf(
            ProfileStatsAdapter.ProfileStatsData(context.getString(R.string.profile_anime_stats)),
            ProfileStatsAdapter.ProfileStatsData(context.getString(R.string.profile_manga_stats))
        )
        adapter = ProfileStatsAdapter(tabItems)
        adapter.isChartAnimationEnabled = isInitialCreation

        viewPager.apply {
            adapter = this@ProfileStatsSection.adapter
            recyclerView.isNestedScrollingEnabled = false
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                ProfileStatsAdapter.POS_ANIME -> tab.setText(R.string.profile_anime_stats)
                ProfileStatsAdapter.POS_MANGA -> tab.setText(R.string.profile_manga_stats)
            }
        }.attach()
    }

    fun submitStats(stats: List<UserStats>?, animateChart: Boolean) {
        adapter.isChartAnimationEnabled = animateChart
        val animeCategoryStats: UserStatsData.CategoryBreakdownData? =
            stats.findStatsData(UserStatsKind.AnimeCategoryBreakdown)
        updateStatsChart(
            ProfileStatsAdapter.POS_ANIME,
            R.string.profile_anime_stats,
            animeCategoryStats
        )

        val mangaCategoryStats: UserStatsData.CategoryBreakdownData? =
            stats.findStatsData(UserStatsKind.MangaCategoryBreakdown)
        updateStatsChart(
            ProfileStatsAdapter.POS_MANGA,
            R.string.profile_manga_stats,
            mangaCategoryStats
        )

        val animeAmountConsumed: UserStatsData.AmountConsumedData? =
            stats.findStatsData(UserStatsKind.AnimeAmountConsumed)
        adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_ANIME, animeAmountConsumed)

        val mangaAmountConsumed: UserStatsData.AmountConsumedData? =
            stats.findStatsData(UserStatsKind.MangaAmountConsumed)
        adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_MANGA, mangaAmountConsumed)
    }

    fun setLoading(isLoading: Boolean) {
        adapter.setLoading(ProfileStatsAdapter.POS_ANIME, isLoading)
        adapter.setLoading(ProfileStatsAdapter.POS_MANGA, isLoading)
    }

    private inline fun <reified T> List<UserStats>?.findStatsData(kind: UserStatsKind): T? {
        return this?.find { it.kind == kind }?.statsData as? T
    }

    private fun updateStatsChart(
        position: Int,
        @StringRes titleRes: Int,
        categoryStats: UserStatsData.CategoryBreakdownData?
    ) {
        val categoryEntries: List<PieEntry> = categoryStats?.let { stats ->
            val total = stats.total ?: return@let null
            val categories = stats.categories ?: return@let null
            if (total <= 0) return@let null

            val sorted = categories.toList()
                .filter { it.second != 0 }
                .sortedByDescending { it.second }

            sorted.take(PieChartStyle.STATS_MAX_ELEMENTS)
                .map { (category, value) ->
                    PieEntry(value.toFloat() / total * 100f, category)
                }
        } ?: emptyList()

        val pieDataSet = PieDataSet(categoryEntries, context.getString(titleRes))
        adapter.updateCategoryData(position, pieDataSet)
    }
}
