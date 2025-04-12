package io.github.drumber.kitsune.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsKind
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.databinding.ItemProfileSiteChipBinding
import io.github.drumber.kitsune.ui.adapter.CharacterAdapter
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.component.chart.PieChartStyle
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.round

class ProfileFragment : BaseFragment(R.layout.fragment_profile, true),
    NavigationBarView.OnItemReselectedListener {

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

        initToolbar()
        updateOptionsMenu()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                updateUser(user)
                updateProfileLinks(user?.profileLinks ?: emptyList())
                updateOptionsMenu()
                binding.swipeRefreshLayout.isEnabled = user != null
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.swipeRefreshLayout.apply {
                    isRefreshing = isRefreshing && state.isRefreshing
                }
            }
        }

        binding.apply {
            btnLogin.setOnClickListener {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }

            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            nsvContent.initPaddingWindowInsetsListener(bottom = true, consume = false)

            swipeRefreshLayout.apply {
                setAppTheme()
                setOnRefreshListener {
                    viewModel.refreshUser()
                }
            }

            ivCover.setOnClickListener {
                val coverImgUrl = viewModel.getUser()?.coverImage?.originalOrDown()
                    ?: return@setOnClickListener
                val title = viewModel.getUser()?.name?.let { "$it Cover" }
                openPhotoViewActivity(coverImgUrl, title, null, ivCover)
            }

            val onWaifuClicked: OnClickListener = object : OnClickListener {
                override fun onClick(v: View?) {
                    val waifu = viewModel.getUser()?.waifu ?: return
                    openCharacterDetailsBottomSheet(waifu)
                }
            }
            layoutWaifuRow.root.setOnClickListener(onWaifuClicked)
            layoutWaifuRow.tvValue.setOnClickListener(onWaifuClicked)
        }

        initStatsViewPager()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val savedState = findNavController().currentBackStackEntry?.savedStateHandle
                savedState?.getStateFlow("refreshFavorites", false)
                    ?.collectLatest { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.refreshUser()
                            savedState["refreshFavorites"] = false
                        }
                    }
            }
        }
    }

    private fun initToolbar() {
        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(consume = false)
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> {
                        val action = ProfileFragmentDirections
                            .actionProfileFragmentToSettingsNavGraph()
                        findNavController().navigate(action)
                    }

                    R.id.menu_edit_profile -> {
                        val action = ProfileFragmentDirections
                            .actionProfileFragmentToEditProfileFragment()
                        findNavController().navigateSafe(R.id.profile_fragment, action)
                    }

                    R.id.menu_share_profile_url -> {
                        val user = viewModel.getUser()
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
        }
    }

    private fun setToolbarLogoClickListener() {
        binding.toolbar.children.firstOrNull { it is ImageView }?.setOnClickListener { logoView ->
            val avatarImgUrl = viewModel.getUser()?.avatar?.originalOrDown()
                ?: return@setOnClickListener
            val title = viewModel.getUser()?.name?.let { "$it Avatar" }
            openPhotoViewActivity(avatarImgUrl, title, null, logoView)
        }
    }

    private fun updateUser(user: User?) {
        binding.user = user
        binding.invalidateAll()

        val glide = Glide.with(this)

        glide.load(user?.avatar?.originalOrDown())
            .dontAnimate()
            .circleCrop()
            .override(45.toPx())
            .placeholder(R.drawable.profile_picture_placeholder)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.toolbar.logo = resource
                    setToolbarLogoClickListener()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        glide.load(user?.coverImage?.originalOrDown())
            .centerCrop()
            .placeholder(ColorDrawable(SurfaceColors.SURFACE_0.getColor(requireContext())))
            .into(binding.ivCover)

        user?.waifu?.let { waifu ->
            glide.asBitmap()
                .load(waifu.image?.originalOrDown())
                .circleCrop()
                .dontAnimate()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.layoutWaifuRow.icon = BitmapDrawable(resources, resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                val animeCategoryStats: UserStatsData.CategoryBreakdownData? = user?.stats
                    .findStatsData(UserStatsKind.AnimeCategoryBreakdown)
                updateStatsChart(
                    ProfileStatsAdapter.POS_ANIME,
                    R.string.profile_anime_stats,
                    animeCategoryStats
                )

                val mangaCategoryStats: UserStatsData.CategoryBreakdownData? = user?.stats
                    .findStatsData(UserStatsKind.MangaCategoryBreakdown)
                updateStatsChart(
                    ProfileStatsAdapter.POS_MANGA,
                    R.string.profile_manga_stats,
                    mangaCategoryStats
                )

                val animeAmountConsumed: UserStatsData.AmountConsumedData? = user?.stats
                    .findStatsData(UserStatsKind.AnimeAmountConsumed)
                adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_ANIME, animeAmountConsumed)

                val mangaAmountConsumed: UserStatsData.AmountConsumedData? = user?.stats
                    .findStatsData(UserStatsKind.MangaAmountConsumed)
                adapter.updateAmountConsumedData(ProfileStatsAdapter.POS_MANGA, mangaAmountConsumed)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                adapter.setLoading(ProfileStatsAdapter.POS_ANIME, state.isInitialLoading)
                adapter.setLoading(ProfileStatsAdapter.POS_MANGA, state.isInitialLoading)
            }
        }
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

    private fun updateProfileLinks(profileLinks: List<ProfileLink>) {
        binding.scrollViewProfileLinks.isVisible = profileLinks.isNotEmpty()
        binding.chipGroupProfileLinks.apply {
            removeAllViews()

            profileLinks.sortedBy { it.profileLinkSite?.id?.toIntOrNull() }
                .forEach { profileLink ->
                    val profileLinkBinding = ItemProfileSiteChipBinding.inflate(
                        layoutInflater,
                        this,
                        true
                    )
                    val chip = profileLinkBinding.root
                    val siteName = profileLink.profileLinkSite?.name
                    chip.text = siteName
                    chip.setChipIconResource(getProfileSiteLogoResourceId(siteName))
                    chip.setOnClickListener {
                        onProfileLinkClicked(profileLink)
                    }
                }
        }
    }

    private fun updateFavoritesData(favorites: List<Favorite>) {
        val favAnime = favorites.filter { it.item is Anime }.map { it.item as Anime }
        val favManga = favorites.filter { it.item is Manga }.map { it.item as Manga }
        val favCharacters =
            favorites.filter { it.item is Character }.map { it.item as Character }

        showFavoriteMediaInRecyclerView(binding.rvFavoriteAnime, favAnime)
        showFavoriteMediaInRecyclerView(binding.rvFavoriteManga, favManga)
        showFavoriteCharactersInRecyclerView(binding.rvFavoriteCharacters, favCharacters)

        binding.layoutFavoriteAnime.isVisible = favAnime.isNotEmpty()
        binding.layoutFavoriteManga.isVisible = favManga.isNotEmpty()
        binding.layoutFavoriteCharacters.isVisible = favCharacters.isNotEmpty()
    }

    private fun showFavoriteMediaInRecyclerView(
        recyclerView: RecyclerView,
        data: List<Media>
    ) {
        if (recyclerView.adapter !is MediaRecyclerViewAdapter) {
            val glide = Glide.with(this)
            val adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                itemSize = MediaItemSize.SMALL
            ) { view, media ->
                onFavoriteMediaItemClicked(view, media)
            }
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
            val glide = Glide.with(this)
            val adapter = CharacterAdapter(
                CopyOnWriteArrayList(data),
                glide,
            ) { _, character ->
                openCharacterDetailsBottomSheet(character)
            }
            recyclerView.adapter = adapter
        } else {
            val adapter = recyclerView.adapter as CharacterAdapter
            adapter.dataSet.clear()
            adapter.dataSet.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFavoriteMediaItemClicked(view: View, media: Media) {
        val action =
            ProfileFragmentDirections.actionProfileFragmentToDetailsFragment(media.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.profile_fragment, action, extras)
    }

    private fun openCharacterDetailsBottomSheet(character: Character) {
        val action =
            ProfileFragmentDirections.actionProfileFragmentToCharacterDetailsBottomSheet(
                character.toCharacterDto()
            )
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }

    private fun updateOptionsMenu() {
        val isLoggedIn = viewModel.getUser() != null
        binding.toolbar.menu.apply {
            findItem(R.id.menu_edit_profile).isVisible = isLoggedIn
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
                onLogOut()
                dialog.dismiss()
            }
            .show()
    }

    private fun onLogOut() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.logOut()
        }
    }

    private fun onProfileLinkClicked(profileLink: ProfileLink) {
        profileLink.url?.let { url ->
            if (URLUtil.isValidUrl(url)) {
                openUrl(url)
            } else {
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText(profileLink.profileLinkSite?.name ?: "URL", url)
                clipboard?.setPrimaryClip(clip)
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.nsvContent.smoothScrollTo(0, 0)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}