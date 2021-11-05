package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.stats.StatsData
import io.github.drumber.kitsune.data.model.stats.StatsKind
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.ui.widget.PieChartStyle
import io.github.drumber.kitsune.ui.widget.ProfilePictureBehavior
import io.github.drumber.kitsune.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.round

class ProfileFragment : BaseFragment(R.layout.fragment_profile, true) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            binding.user = user

            val glide = GlideApp.with(this)

            glide.load(user?.avatar)
                .dontAnimate()
                .placeholder(R.drawable.profile_picture_placeholder)
                .into(binding.ivProfileImage)

            glide.load(user?.coverImage)
                .centerCrop()
                .placeholder(R.drawable.cover_placeholder)
                .into(binding.ivCover)
        }

        binding.apply {
            btnSettings.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }

            appBarLayout.addOnOffsetChangedListener(
                FadingToolbarOffsetListener(
                    requireActivity(),
                    toolbar
                )
            )

            ViewCompat.setOnApplyWindowInsetsListener(collapsingToolbar) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                collapsingToolbar.expandedTitleMarginStart = insets.left +
                        resources.getDimensionPixelSize(R.dimen.profile_text_offset_expanded)
                windowInsets
            }
            coverSpacer.initMarginWindowInsetsListener(
                left = true,
                top = true,
                right = true,
                consume = false
            )
            toolbar.initWindowInsetsListener(consume = false)

            ViewCompat.setOnApplyWindowInsetsListener(ivProfileImage) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val params = ivProfileImage.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior as ProfilePictureBehavior
                behavior.offsetX = insets.left.toFloat()
                behavior.offsetY = insets.top.toFloat()
                windowInsets
            }

            nsvContent.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        }

        initStatsViewPager()
    }

    private fun initStatsViewPager() {
        val dataSet = listOf(
            PieChartAdapter.PieChartData(getString(R.string.profile_anime_stats)),
            PieChartAdapter.PieChartData(getString(R.string.profile_manga_stats))
        )
        binding.viewPagerStats.apply {
            adapter = PieChartAdapter(dataSet)
            recyclerView.isNestedScrollingEnabled = false
        }

        TabLayoutMediator(binding.tabLayoutStats, binding.viewPagerStats) { tab, position ->
            when (position) {
                0 -> tab.setText(R.string.profile_anime_stats)
                1 -> tab.setText(R.string.profile_manga_stats)
            }
        }.attach()

        viewModel.fullUserModel.observe(viewLifecycleOwner) { user ->
            val animeCategoryStats = user.stats?.find {
                it.kind == StatsKind.AnimeCategoryBreakdown
            }?.statsData as? StatsData.CategoryBreakdownData
            updateStatsChart(0, R.string.profile_anime_stats, animeCategoryStats)

            val mangaCategoryStats = user.stats?.find {
                it.kind == StatsKind.MangaCategoryBreakdown
            }?.statsData as? StatsData.CategoryBreakdownData
            updateStatsChart(1, R.string.profile_manga_stats, mangaCategoryStats)
        }
    }

    private fun updateStatsChart(
        position: Int,
        @StringRes titleRes: Int,
        categoryStats: StatsData.CategoryBreakdownData?
    ) {
        val categoryEntries: List<PieEntry> = categoryStats?.let { stats ->
            val total = stats.total ?: return@let null
            val categories = stats.categories ?: return@let null
            categories.toList()
                .filter { it.second != 0 }
                .sortedByDescending { it.second }
                .take(PieChartStyle.STATS_MAX_ELEMENTS)
                .map { (category, value) -> PieEntry(round(value.toFloat() / total * 100f), category) }
        } ?: emptyList()

        val set = PieDataSet(categoryEntries, getString(titleRes))

        val adapter = binding.viewPagerStats.adapter as PieChartAdapter
        adapter.updateData(position, set)
        adapter.setLoading(position, false)
    }

    override fun onResume() {
        super.onResume()
        if (context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}