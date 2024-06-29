package io.github.drumber.kitsune.ui.profile

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
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
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
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.databinding.ItemProfileSiteChipBinding
import io.github.drumber.kitsune.domain_old.model.infrastructure.character.Character
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.Manga
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.Favorite
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats.Stats
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats.StatsData
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats.StatsKind
import io.github.drumber.kitsune.domain_old.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.domain_old.model.ui.media.originalOrDown
import io.github.drumber.kitsune.ui.adapter.CharacterAdapter
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.chart.PieChartStyle
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.network.ResponseData
import io.github.drumber.kitsune.util.ui.getProfileSiteLogoResourceId
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
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

        viewModel.userModel.observe(viewLifecycleOwner) { user ->
            updateUser(user)
            updateOptionsMenu()
            binding.swipeRefreshLayout.isEnabled = user != null
        }

        viewModel.fullUserModel.observe(viewLifecycleOwner) { fullUser ->
            if (fullUser is ResponseData.Success) {
                updateUser(fullUser.data)
                updateProfileLinks(fullUser.data.profileLinks ?: emptyList())
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

            ivCover.setOnClickListener {
                val coverImgUrl = (viewModel.fullUserModel.value?.data?.coverImage
                    ?: viewModel.userModel.value?.coverImage)?.originalOrDown()
                    ?: return@setOnClickListener
                val title = (viewModel.fullUserModel.value?.data?.name
                    ?: viewModel.userModel.value?.name)?.let { "$it Cover" }
                openPhotoViewActivity(coverImgUrl, title, null, ivCover)
            }

            val onWaifuClicked: OnClickListener = object : OnClickListener {
                override fun onClick(v: View?) {
                    val waifu = viewModel.fullUserModel.value?.data?.waifu
                        ?: viewModel.userModel.value?.waifu
                        ?: return
                    openCharacterDetailsBottomSheet(waifu)
                }
            }
            layoutWaifuRow.root.setOnClickListener(onWaifuClicked)
            layoutWaifuRow.tvValue.setOnClickListener(onWaifuClicked)
        }

        initStatsViewPager()
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
        }
    }

    private fun setToolbarLogoClickListener() {
        binding.toolbar.children.firstOrNull { it is ImageView }?.setOnClickListener { logoView ->
            val avatarImgUrl = (viewModel.fullUserModel.value?.data?.avatar
                ?: viewModel.userModel.value?.avatar)?.originalOrDown()
                ?: return@setOnClickListener
            val title = (viewModel.fullUserModel.value?.data?.name
                ?: viewModel.userModel.value?.name)?.let { "$it Avatar" }
            openPhotoViewActivity(avatarImgUrl, title, null, logoView)
        }
    }

    private fun updateUser(user: LocalUser?) {
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

    private fun updateProfileLinks(profileLinks: List<ProfileLink>) {
        binding.scrollViewProfileLinks.isVisible = profileLinks.isNotEmpty()
        binding.chipGroupProfileLinks.apply {
            removeAllViews()

            profileLinks.sortedBy { it.profileLinkSite?.id?.toIntOrNull() }.forEach { profileLink ->
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
                    profileLink.url?.let { url -> openUrl(url) }
                }
            }
        }
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
            val glide = Glide.with(this)
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

    private fun onFavoriteMediaItemClicked(view: View, mediaAdapter: MediaAdapter) {
        val action = ProfileFragmentDirections.actionProfileFragmentToDetailsFragment(mediaAdapter)
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.profile_fragment, action, extras)
    }

    private fun openCharacterDetailsBottomSheet(character: Character) {
        val action = ProfileFragmentDirections.actionProfileFragmentToCharacterDetailsBottomSheet(character)
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }

    private fun updateOptionsMenu() {
        val isLoggedIn = viewModel.userModel.value != null
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
        viewModel.logOut()
        (requireActivity() as BaseActivity).apply {
            startNewMainActivity()
            finish()
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