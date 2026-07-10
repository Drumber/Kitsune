package io.github.drumber.kitsune.ui.profile

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class BaseProfileFragment : BaseFragment(R.layout.fragment_profile, true),
    FeedListFragment.FeedListParent,
    NavigationBarView.OnItemReselectedListener {

    protected val binding by viewBinding(FragmentProfileBinding::bind)

    protected abstract val viewModel: BaseProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        initToolbar()
        initProfileViewPager()

        binding.apply {
            swipeRefreshLayout.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
            nsvContent.initPaddingWindowInsetsListener(bottom = true, consume = false)

            swipeRefreshLayout.apply {
                setAppTheme()
                setOnRefreshListener {
                    val currentPageFragment = childFragmentManager
                        .findFragmentByTag("f" + binding.viewPagerProfile.currentItem)
                    if (currentPageFragment is FeedListFragment) {
                        currentPageFragment.refreshFeedContent()
                    } else {
                        viewModel.refreshUser()
                    }
                }
            }

            ivCover.setOnClickListener {
                val coverImgUrl = viewModel.getUser()?.coverImage?.originalOrDown()
                    ?: return@setOnClickListener
                val title = viewModel.getUser()?.name?.let { "$it Cover" }
                openPhotoViewActivity(coverImgUrl, title, null, ivCover)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                onUserModelChanged(user)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.swipeRefreshLayout.apply {
                    isRefreshing = isRefreshing && state.isRefreshing
                }
                binding.progressBarProfile.isVisible = state.isInitialLoading
            }
        }
    }

    protected open fun onUserModelChanged(user: User?) {
        binding.user = user
        binding.invalidateAll()
        binding.swipeRefreshLayout.isEnabled = user != null
        binding.swipeRefreshLayout.isVisible = user != null
        binding.tabLayoutProfile.isVisible = user != null
        updateUserAvatarAndCover(user)
    }

    protected open fun initToolbar() {
        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(consume = false)
        }
    }

    protected abstract fun createProfileViewPagerAdapter(userId: String): ProfileViewPagerAdapter

    private fun initProfileViewPager() {
        var viewPagerAdapter: ProfileViewPagerAdapter? = null
        var tabLayoutMediator: TabLayoutMediator? = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.map { it?.id }.distinctUntilChanged().collectLatest { userId ->
                if (userId == null) {
                    tabLayoutMediator?.detach()
                    binding.viewPagerProfile.adapter = null
                    viewPagerAdapter = null
                    tabLayoutMediator = null
                    togglePostFab(false)
                    return@collectLatest
                }

                viewPagerAdapter = createProfileViewPagerAdapter(userId)
                binding.viewPagerProfile.adapter = viewPagerAdapter

                tabLayoutMediator = TabLayoutMediator(binding.tabLayoutProfile, binding.viewPagerProfile) { tab, position ->
                    when (position) {
                        0 -> tab.setText(R.string.profile_tab_about)
                        1 -> tab.setText(R.string.profile_tab_posts)
                    }
                }.also { it.attach() }
                togglePostFab(binding.tabLayoutProfile.selectedTabPosition == 1)
            }
        }

        binding.tabLayoutProfile.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val isPostsTab = tab.position == 1
                togglePostFab(isPostsTab && viewModel.getUser() != null)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun togglePostFab(visible: Boolean) {
        if (visible) {
            binding.fabPostWall.show()
        } else {
            binding.fabPostWall.hide()
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

    private fun updateUserAvatarAndCover(user: User?) {
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
    }

    override fun onDataLoadFinished() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        val currentPageFragment = childFragmentManager
            .findFragmentByTag("f" + binding.viewPagerProfile.currentItem)
        if (currentPageFragment is NavigationBarView.OnItemReselectedListener) {
            currentPageFragment.onNavigationItemReselected(item)
        }
    }
}
