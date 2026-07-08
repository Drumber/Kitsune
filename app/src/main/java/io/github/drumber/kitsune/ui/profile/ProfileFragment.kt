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
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment(R.layout.fragment_profile, true),
    NavigationBarView.OnItemReselectedListener {

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val viewModel: ProfileViewModel by viewModel()

    private val statsSection by lazy {
        ProfileStatsSection(binding.viewPagerStats, binding.tabLayoutStats)
    }

    private val favoritesSection by lazy {
        ProfileFavoritesSection(
            binding = binding,
            glide = Glide.with(this),
            onMediaClick = { view, media -> onFavoriteMediaItemClicked(view, media) },
            onCharacterClick = { character -> openCharacterDetailsBottomSheet(character) }
        )
    }

    private val linksSection by lazy {
        ProfileLinksSection(binding, layoutInflater) { onProfileLinkClicked(it) }
    }

    private var isPostsTab = false

    private var isFeedInitialized = false

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
                binding.layoutNotLoggedIn.isVisible = user == null
                binding.tabLayoutProfile.isVisible = user != null
                if (user?.id != null && !isFeedInitialized) {
                    initProfileFeed(user.id)
                } else if (user == null) {
                    isPostsTab = false
                    if (binding.tabLayoutProfile.selectedTabPosition != 0) {
                        binding.tabLayoutProfile.getTabAt(0)?.select()
                    }
                    binding.swipeRefreshLayout.isVisible = true
                    binding.feedContainer.isVisible = false
                    childFragmentManager.findFragmentById(R.id.feed_container)?.let {
                        childFragmentManager.beginTransaction().remove(it).commit()
                    }
                    isFeedInitialized = false
                }
                updateFabVisibility()
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

            btnFollowing.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWING)
            }
            btnFollowers.setOnClickListener {
                navigateToFollowList(FollowListType.FOLLOWERS)
            }
        }

        initStatsViewPager()
        initProfileTabs()
        val selectedTab = savedInstanceState?.getInt(KEY_SELECTED_TAB, 0) ?: 0
        if (selectedTab != binding.tabLayoutProfile.selectedTabPosition) {
            binding.tabLayoutProfile.getTabAt(selectedTab)?.select()
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB, binding.tabLayoutProfile.selectedTabPosition)
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

    private fun initProfileTabs() {
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
            val action = ProfileFragmentDirections.actionGlobalCreatePostFragment()
            findNavController().navigateSafe(R.id.profile_fragment, action)
        }
    }

    private fun initProfileFeed(userId: String) {
        if (childFragmentManager.findFragmentById(R.id.feed_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.feed_container,
                    FeedListFragment.newUserFeedInstance(userId, R.id.profile_fragment)
                )
                .commit()
        }
        isFeedInitialized = true
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

    private fun updateFabVisibility() {
        binding.fabPostWall.isVisible = isPostsTab && viewModel.getUser() != null
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
                copyToClipboard(profileLink.profileLinkSite?.name ?: "URL", url)
            }
        }
    }

    private fun navigateToFollowList(type: FollowListType) {
        val user = viewModel.getUser() ?: return
        val action = FollowListFragmentDirections.actionGlobalFollowListFragment(
            user.id,
            type,
            user.name
        )
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (isPostsTab) {
            val feedFragment =
                childFragmentManager.findFragmentById(R.id.feed_container) as? FeedListFragment
            feedFragment?.scrollToTopOrRefresh(
                appBarExpanded = binding.appBarLayout.bottom >= binding.appBarLayout.height
            )
            return
        }

        val isAtTop = binding.nsvContent.scrollY == 0 &&
                binding.appBarLayout.bottom >= binding.appBarLayout.height
        if (isAtTop && binding.swipeRefreshLayout.isEnabled) {
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.refreshUser()
        } else {
            binding.nsvContent.smoothScrollTo(0, 0)
            binding.appBarLayout.setExpanded(true)
        }
    }

    companion object {
        private const val TAB_POSTS = 1
        private const val KEY_SELECTED_TAB = "profile_selected_tab"
    }
}
