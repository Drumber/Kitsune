package io.github.drumber.kitsune.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.data.model.production.Character
import io.github.drumber.kitsune.data.model.stats.Stats
import io.github.drumber.kitsune.data.model.stats.StatsData
import io.github.drumber.kitsune.data.model.stats.StatsKind
import io.github.drumber.kitsune.data.model.user.Favorite
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.adapter.CharacterAdapter
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.FadingToolbarOffsetListener
import io.github.drumber.kitsune.ui.widget.ProfilePictureBehavior
import io.github.drumber.kitsune.ui.widget.chart.PieChartStyle
import io.github.drumber.kitsune.util.extensions.clearLightStatusBar
import io.github.drumber.kitsune.util.extensions.isLightStatusBar
import io.github.drumber.kitsune.util.extensions.isNightMode
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openCharacterOnMAL
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.setLightStatusBar
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.initWindowInsetsListener
import io.github.drumber.kitsune.util.network.ResponseData
import io.github.drumber.kitsune.util.originalOrDown
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList
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
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        updateOptionsMenu()

        if (context?.isNightMode() == false) {
            activity?.clearLightStatusBar()
        }

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            updateUser(user)
            updateOptionsMenu()
        }

        viewModel.fullUserModel.observe(viewLifecycleOwner) { fullUser ->
            if (fullUser is ResponseData.Success) {
                updateUser(fullUser.data)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.apply {
                isRefreshing = isRefreshing && isLoading
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
                        val action = ProfileFragmentDirections
                            .actionProfileFragmentToSettingsNavGraph()
                        findNavController().navigate(action)
                    }
                    R.id.menu_share_profile_url -> {
                        val user = viewModel.userModel.value
                        val profileId = user?.slug ?: user?.id
                        if (profileId != null) {
                            val url = Kitsu.USER_URL_PREFIX + profileId
                            startUrlShareIntent(url)
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

            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )

            swipeRefreshLayout.apply {
                setAppTheme()
                setOnRefreshListener {
                    viewModel.refreshUser()
                }
            }

            ivProfileImage.setOnClickListener {
                val avatarImgUrl = (viewModel.fullUserModel.value?.data?.avatar
                    ?: viewModel.userModel.value?.avatar)?.originalOrDown()
                    ?: return@setOnClickListener
                val title = (viewModel.fullUserModel.value?.data?.name
                    ?: viewModel.userModel.value?.name)?.let { "$it Avatar" }
                openImageViewer(avatarImgUrl, title, null, ivProfileImage)
            }

            ivCover.setOnClickListener {
                val coverImgUrl = (viewModel.fullUserModel.value?.data?.coverImage
                    ?: viewModel.userModel.value?.coverImage)?.originalOrDown()
                    ?: return@setOnClickListener
                val title = (viewModel.fullUserModel.value?.data?.name
                    ?: viewModel.userModel.value?.name)?.let { "$it Cover" }
                openImageViewer(coverImgUrl, title, null, ivCover)
            }
        }

        initStatsViewPager()
    }

    private fun updateUser(user: User?) {
        binding.user = user
        binding.invalidateAll()

        val glide = GlideApp.with(this)

        glide.load(user?.avatar?.originalOrDown())
            .dontAnimate()
            .placeholder(R.drawable.profile_picture_placeholder)
            .into(binding.ivProfileImage)

        glide.load(user?.coverImage?.originalOrDown())
            .centerCrop()
            .placeholder(R.drawable.cover_placeholder)
            .into(binding.ivCover)

        user?.favorites?.let { updateFavoritesData(it) }
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

            val animeCategoryStats: StatsData.CategoryBreakdownData? = user
                ?.stats
                .findStatsData(StatsKind.AnimeCategoryBreakdown)
            updateStatsChart(
                ProfileStatsAdapter.POS_ANIME,
                R.string.profile_anime_stats,
                animeCategoryStats
            )

            val mangaCategoryStats: StatsData.CategoryBreakdownData? = user
                ?.stats
                .findStatsData(StatsKind.MangaCategoryBreakdown)
            updateStatsChart(
                ProfileStatsAdapter.POS_MANGA,
                R.string.profile_manga_stats,
                mangaCategoryStats
            )

            val animeAmountConsumed: StatsData.AmountConsumedData? = user
                ?.stats
                .findStatsData(StatsKind.AnimeAmountConsumed)
            adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_ANIME, animeAmountConsumed)

            val mangaAmountConsumed: StatsData.AmountConsumedData? = user
                ?.stats
                .findStatsData(StatsKind.MangaAmountConsumed)
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
                .map { (category, value) ->
                    PieEntry(
                        round(value.toFloat() / total * 100f),
                        category
                    )
                }
        } ?: emptyList()

        val set = PieDataSet(categoryEntries, getString(titleRes))

        val adapter = binding.viewPagerStats.adapter as ProfileStatsAdapter
        adapter.updateCategoryData(position, set)
    }

    private fun updateFavoritesData(favorites: List<Favorite>) {
        val favAnime = favorites
            .mapNotNull { (it.item as? Anime)?.let { media -> MediaAdapter.fromMedia(media) } }
        val favManga = favorites
            .mapNotNull { (it.item as? Manga)?.let { media -> MediaAdapter.fromMedia(media) } }
        val favCharacters = favorites
            .mapNotNull { it.item as? Character }

        showFavoriteMediaInRecyclerView(binding.rvFavoriteAnime, favAnime)
        showFavoriteMediaInRecyclerView(binding.rvFavoriteManga, favManga)
        showFavoriteCharactersInRecyclerView(binding.rvFavoriteCharacters, favCharacters)

        binding.layoutFavoriteAnime.isVisible = favAnime.isNotEmpty()
        binding.layoutFavoriteManga.isVisible = favManga.isNotEmpty()
        binding.layoutFavoriteCharacters.isVisible = favCharacters.isNotEmpty()
    }

    private fun showFavoriteMediaInRecyclerView(
        recyclerView: RecyclerView,
        data: List<MediaAdapter>
    ) {
        if (recyclerView.adapter !is MediaRecyclerViewAdapter) {
            val glide = GlideApp.with(this)
            val adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                MediaViewHolder.TagData.RelationshipRole
            ) { view, media ->
                onFavoriteMediaItemClicked(view, media)
            }
            adapter.overrideItemSize = MediaItemSize.SMALL
            recyclerView.adapter = adapter
        } else {
            val adapter = recyclerView.adapter as MediaRecyclerViewAdapter
            adapter.dataSet.clear()
            adapter.dataSet.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showFavoriteCharactersInRecyclerView(
        recyclerView: RecyclerView,
        data: List<Character>
    ) {
        if (recyclerView.adapter !is CharacterAdapter) {
            val glide = GlideApp.with(this)
            val adapter = CharacterAdapter(
                CopyOnWriteArrayList(data),
                glide,
            ) { _, character ->
                onFavoriteCharacterItemClicked(character)
            }
            recyclerView.adapter = adapter
        } else {
            val adapter = recyclerView.adapter as CharacterAdapter
            adapter.dataSet.clear()
            adapter.dataSet.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFavoriteMediaItemClicked(view: View, mediaAdapter: MediaAdapter) {
        val action = ProfileFragmentDirections.actionProfileFragmentToDetailsFragment(mediaAdapter)
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.profile_fragment, action, extras)
    }

    private fun onFavoriteCharacterItemClicked(character: Character) {
        character.malId?.let { malId ->
            openCharacterOnMAL(malId)
        }
    }

    private fun updateOptionsMenu() {
        val isLoggedIn = viewModel.userModel.value != null
        binding.toolbar.menu.apply {
            findItem(R.id.menu_log_out).isVisible = isLoggedIn
            findItem(R.id.menu_share_profile_url).isVisible = isLoggedIn
        }
    }

    private fun openImageViewer(imageUrl: String, title: String?, thumbnailUrl: String?, sharedElement: View?) {
        val transitionName = sharedElement?.let { ViewCompat.getTransitionName(it) }
        val action = ProfileFragmentDirections.actionProfileFragmentToPhotoViewActivity(
            imageUrl,
            title,
            thumbnailUrl,
            transitionName
        )
        val options = if (sharedElement != null && transitionName != null) {
            ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), sharedElement, transitionName)
        } else {
            null
        }
        val extras = ActivityNavigatorExtras(options)
        findNavController().navigate(action, extras)
    }

    private fun showLogOutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_log_out)
            .setMessage(R.string.dialog_log_out_confirmation)
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.action_log_out) { dialog, _ ->
                onLogOut()
                dialog.dismiss()
            }
            .show()
    }

    private fun onLogOut() {
        viewModel.logOut()
        (requireActivity() as BaseActivity).apply {
            startNewMainActivity()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity?.isLightStatusBar() == false && context?.isNightMode() == false) {
            activity?.setLightStatusBar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}