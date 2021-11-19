package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.model.stats.Stats
import io.github.drumber.kitsune.data.model.stats.StatsData
import io.github.drumber.kitsune.data.model.stats.StatsKind
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.ui.widget.PieChartStyle
import io.github.drumber.kitsune.ui.widget.ProfilePictureBehavior
import io.github.drumber.kitsune.util.*
import io.github.drumber.kitsune.util.network.ResponseData
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
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateOptionsMenu()

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            updateUser(user)
            updateOptionsMenu()
        }
        viewModel.fullUserModel.observe(viewLifecycleOwner) { fullUser ->
            if (fullUser is ResponseData.Success) {
                updateUser(fullUser.data)
            }
        }

        binding.apply {
            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> {
                        val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
                        findNavController().navigate(action)
                    }
                    R.id.menu_share_profile_url -> {
                        val user = viewModel.userModel.value
                        val profileId = user?.slug ?: user?.id
                        if (profileId != null) {
                            val url = Kitsu.USER_URL_PREFIX + profileId
                            val shareIntent = Intent.createChooser(Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, url)
                                type = "text/plain"
                            }, null)
                            startActivity(shareIntent)
                        } else {
                            showSomethingWrongToast()
                        }
                    }
                    R.id.menu_log_out -> {
                        showLogOutConfirmationDialog()
                    }
                }
                true
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

    private fun updateUser(user: User?) {
        binding.user = user
        binding.invalidateAll()

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

    private fun initStatsViewPager() {
        val dataSet = listOf(
            ProfileStatsAdapter.ProfileStatsData(getString(R.string.profile_anime_stats)),
            ProfileStatsAdapter.ProfileStatsData(getString(R.string.profile_manga_stats))
        )
        val adapter = ProfileStatsAdapter(dataSet)

        binding.viewPagerStats.apply {
            this.adapter = adapter
            recyclerView.isNestedScrollingEnabled = false
        }

        TabLayoutMediator(binding.tabLayoutStats, binding.viewPagerStats) { tab, position ->
            when (position) {
                ProfileStatsAdapter.POS_ANIME -> tab.setText(R.string.profile_anime_stats)
                ProfileStatsAdapter.POS_MANGA -> tab.setText(R.string.profile_manga_stats)
            }
        }.attach()

        viewModel.fullUserModel.observe(viewLifecycleOwner) { response ->
            val user = response.data

            val animeCategoryStats: StatsData.CategoryBreakdownData? = user?.stats.findStatsData(StatsKind.AnimeCategoryBreakdown)
            updateStatsChart(ProfileStatsAdapter.POS_ANIME, R.string.profile_anime_stats, animeCategoryStats)

            val mangaCategoryStats: StatsData.CategoryBreakdownData? = user?.stats.findStatsData(StatsKind.MangaCategoryBreakdown)
            updateStatsChart(ProfileStatsAdapter.POS_MANGA, R.string.profile_manga_stats, mangaCategoryStats)

            val animeAmountConsumed: StatsData.AmountConsumedData? = user?.stats.findStatsData(StatsKind.AnimeAmountConsumed)
            adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_ANIME, animeAmountConsumed)

            val mangaAmountConsumed: StatsData.AmountConsumedData? = user?.stats.findStatsData(StatsKind.MangaAmountConsumed)
            adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_MANGA, mangaAmountConsumed)

            adapter.setLoading(ProfileStatsAdapter.POS_ANIME, false)
            adapter.setLoading(ProfileStatsAdapter.POS_MANGA, false)
        }
    }

    private inline fun <reified T> List<Stats>?.findStatsData(kind: StatsKind): T? {
        return this?.find { it.kind == kind }?.statsData as? T
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

        val adapter = binding.viewPagerStats.adapter as ProfileStatsAdapter
        adapter.updateCategoryData(position, set)
    }

    private fun updateOptionsMenu() {
        val isLoggedIn = viewModel.userModel.value != null
        binding.toolbar.menu.apply {
            findItem(R.id.menu_log_out).isVisible = isLoggedIn
            findItem(R.id.menu_share_profile_url).isVisible = isLoggedIn
        }
    }

    private fun showLogOutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_log_out)
            .setMessage(R.string.dialog_log_out_confirmation)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.action_log_out) { dialog, _ ->
                viewModel.logOut()
                dialog.dismiss()
            }
            .show()
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