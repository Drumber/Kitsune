package io.github.drumber.kitsune.ui.profile

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
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.tabs.TabLayout
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/** Read-only profile screen for viewing another user's profile. */
class UserProfileFragment : BaseFragment(R.layout.fragment_profile, true) {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val args: UserProfileFragmentArgs by navArgs()

    private val viewModel: UserProfileViewModel by viewModel {
        parametersOf(args.userId)
    }

    private lateinit var statsSection: ProfileStatsSection
    private lateinit var favoritesSection: ProfileFavoritesSection
    private lateinit var linksSection: ProfileLinksSection

    /** Whether the Posts tab is currently selected. */
    private var isPostsTab = false

    /** Whether the logged-in user is allowed to post on this wall. */
    private var canPostOnWall = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        statsSection = ProfileStatsSection(binding.viewPagerStats, binding.tabLayoutStats)
        favoritesSection = ProfileFavoritesSection(
            binding = binding,
            glide = Glide.with(this),
            onMediaClick = { view, media -> onFavoriteMediaItemClicked(view, media) },
            onCharacterClick = { character -> openCharacterDetailsBottomSheet(character) }
        )
        linksSection = ProfileLinksSection(binding, layoutInflater) { onProfileLinkClicked(it) }

        initToolbar()

        binding.apply {
            // Self-only UI is not used in the read-only profile view.
            btnLogin.isVisible = false

            btnFollowing.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWING)
            }
            btnFollowers.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWERS)
            }

            // Show the provided name immediately, before the full profile loads.
            toolbar.title = args.userName ?: getString(R.string.nav_profile)
            collapsingToolbar.title = args.userName ?: getString(R.string.nav_profile)

            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            nsvContent.initPaddingWindowInsetsListener(bottom = true, consume = false)

            swipeRefreshLayout.apply {
                setAppTheme()
                setOnRefreshListener { viewModel.refreshUser() }
            }

            ivCover.setOnClickListener {
                val coverImgUrl = viewModel.getUser()?.coverImage?.originalOrDown()
                    ?: return@setOnClickListener
                val title = viewModel.getUser()?.name?.let { "$it Cover" }
                openPhotoViewActivity(coverImgUrl, title, null, ivCover)
            }

            val onWaifuClicked = OnClickListener {
                val waifu = viewModel.getUser()?.waifu ?: return@OnClickListener
                openCharacterDetailsBottomSheet(waifu)
            }
            layoutWaifuRow.root.setOnClickListener(onWaifuClicked)
            layoutWaifuRow.tvValue.setOnClickListener(onWaifuClicked)

            btnFollow.setOnClickListener { viewModel.toggleFollow() }
        }

        initStatsViewPager()
        initProfileFeed()
        val selectedTab = savedInstanceState?.getInt(KEY_SELECTED_TAB, 0) ?: 0
        if (selectedTab != binding.tabLayoutProfile.selectedTabPosition) {
            binding.tabLayoutProfile.getTabAt(selectedTab)?.select()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                updateUser(user)
                updateProfileLinks(user?.profileLinks ?: emptyList())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.swipeRefreshLayout.isRefreshing = state.isRefreshing
                binding.progressBarProfile.isVisible = state.isInitialLoading
                binding.btnFollow.apply {
                    isVisible = state.canFollow
                    isEnabled = !state.isFollowProcessing
                    setText(
                        if (state.isFollowing) R.string.action_unfollow
                        else R.string.action_follow
                    )
                }
                // A logged-in user (who isn't viewing their own profile) may post on the wall.
                canPostOnWall = state.canFollow
                updateFabVisibility()
            }
        }
    }

    private fun initToolbar() {
        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(consume = false)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            // Hide self-only menu items; keep only the share action.
            toolbar.menu.apply {
                findItem(R.id.menu_edit_profile)?.isVisible = false
                findItem(R.id.menu_settings)?.isVisible = false
                findItem(R.id.menu_log_out)?.isVisible = false
                findItem(R.id.menu_share_profile_url)?.isVisible = true
            }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_profile_url -> {
                        val user = viewModel.getUser()
                        val profileId = user?.slug ?: user?.id ?: args.userId
                        val url = io.github.drumber.kitsune.constants.Kitsu.USER_URL_PREFIX + profileId
                        startUrlShareIntent(url)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, binding.tabLayoutProfile.selectedTabPosition)
    }

    private fun updateUser(user: User?) {
        binding.user = user
        binding.invalidateAll()
        // Flush the data binding synchronously so our manual title/subtitle below
        // win over the shared layout's "user.name ?? not_logged_in" expression.
        binding.executePendingBindings()
        val displayName = user?.name ?: user?.slug ?: args.userName
            ?: getString(R.string.nav_profile)
        // The CollapsingToolbarLayout draws its own title, so set it there directly.
        binding.collapsingToolbar.title = displayName
        binding.toolbar.apply {
            title = displayName
            // Only show the @tag as subtitle when we also have a distinct display name.
            subtitle = user?.slug?.takeIf { !user.name.isNullOrBlank() }?.let { "@$it" }
        }

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

    private fun initProfileFeed() {
        binding.tabLayoutProfile.isVisible = true

        if (childFragmentManager.findFragmentById(R.id.feed_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.feed_container,
                    FeedListFragment.newUserFeedInstance(args.userId, R.id.user_profile_fragment)
                )
                .commit()
        }

        binding.tabLayoutProfile.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isPostsTab = tab.position == TAB_POSTS
                binding.swipeRefreshLayout.isVisible = !isPostsTab
                binding.feedContainer.isVisible = isPostsTab
                updateFabVisibility()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.fabPostWall.setOnClickListener {
            val action = UserProfileFragmentDirections.actionGlobalCreatePostFragment(
                targetUserId = args.userId,
                targetUserName = viewModel.getUser()?.name ?: args.userName
            )
            findNavController().navigateSafe(R.id.user_profile_fragment, action)
        }
    }

    private fun navigateToFollowList(type: FollowListType) {
        val action = FollowListFragmentDirections.actionGlobalFollowListFragment(
            args.userId,
            type,
            viewModel.getUser()?.name ?: args.userName
        )
        findNavController().navigateSafe(R.id.user_profile_fragment, action)
    }

    private fun updateFabVisibility() {
        binding.fabPostWall.isVisible = isPostsTab && canPostOnWall
    }

    private fun initStatsViewPager() {
        statsSection.init()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                statsSection.submitStats(user?.stats)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                statsSection.setLoading(state.isInitialLoading)
            }
        }
    }

    private fun updateProfileLinks(profileLinks: List<ProfileLink>) {
        linksSection.submitProfileLinks(profileLinks)
    }

    private fun updateFavoritesData(favorites: List<Favorite>) {
        favoritesSection.submitFavorites(favorites)
    }

    private fun onFavoriteMediaItemClicked(view: View, media: Media) {
        val action = UserProfileFragmentDirections.actionGlobalDetailsFragment(
            media = media.toMediaDto()
        )
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.user_profile_fragment, action, extras)
    }

    private fun openCharacterDetailsBottomSheet(character: Character) {
        val action = UserProfileFragmentDirections
            .actionGlobalCharacterDetailsBottomSheet(character.toCharacterDto())
        findNavController().navigateSafe(R.id.user_profile_fragment, action)
    }

    private fun onProfileLinkClicked(profileLink: ProfileLink) {
        profileLink.url?.let { url ->
            if (URLUtil.isValidUrl(url)) {
                openUrl(url)
            } else {
                copyToClipboard(profileLink.profileLinkSite?.name ?: "URL", url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoritesSection.clear()
    }

    companion object {
        private const val TAB_POSTS = 1
        private const val KEY_SELECTED_TAB = "user_profile_selected_tab"
    }
}
