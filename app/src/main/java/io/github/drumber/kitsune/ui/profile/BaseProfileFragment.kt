package io.github.drumber.kitsune.ui.profile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.load
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.databinding.FragmentProfileBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

abstract class BaseProfileFragment : BaseFragment(R.layout.fragment_profile, true),
    NavigationBarView.OnItemReselectedListener {

    protected val binding by viewBinding(FragmentProfileBinding::bind)

    protected abstract val viewModel: BaseProfileViewModel

    protected abstract val useSocialImageLoader: Boolean

    private val imageLoader: ImageLoader
        get() = when (useSocialImageLoader) {
            true -> get(named<SocialImagesLoader>())
            false -> SingletonImageLoader.get(requireContext())
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        initToolbar()
        initProfileViewPager()

        binding.apply {
            ivCover.setOnClickListener {
                val coverImgUrl = viewModel.getUser()?.coverImage?.originalOrDown()
                    ?: return@setOnClickListener
                val title = viewModel.getUser()?.name?.let { "$it Cover" }
                openPhotoViewActivity(
                    coverImgUrl,
                    title,
                    null,
                    ivCover,
                    useSocialImageLoader = useSocialImageLoader
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userModel.collectLatest { user ->
                onUserModelChanged(user)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBarProfile.isVisible =
                    state.isInitialLoading && viewModel.getUser() == null
            }
        }
    }

    protected open fun onUserModelChanged(user: User?) {
        binding.user = user
        binding.invalidateAll()
        binding.tabLayoutProfile.isVisible = user != null
        binding.viewPagerProfile.isVisible = user != null
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
                binding.viewPagerProfile.recyclerView.isNestedScrollingEnabled = false

                tabLayoutMediator = TabLayoutMediator(
                    binding.tabLayoutProfile,
                    binding.viewPagerProfile
                ) { tab, position ->
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
            val user = viewModel.getUser()
            val avatarImgUrl = user?.avatar?.originalOrDown()
                ?: return@setOnClickListener
            val title = viewModel.getUser()?.name?.let { "$it Avatar" }
            openPhotoViewActivity(
                avatarImgUrl,
                title,
                user.avatar.largeOrDown(),
                logoView,
                useSocialImageLoader = useSocialImageLoader
            )
        }
    }

    private fun updateUserAvatarAndCover(user: User?) {
        val request = ImageRequest.Builder(requireContext())
            .data(user?.avatar?.largeOrDown())
            .size(45.toPx())
            .transformations(CircleCropTransformation())
            .crossfade(false)
            .placeholder(R.drawable.profile_picture_placeholder)
            .error(R.drawable.profile_picture_placeholder)
            .fallback(R.drawable.profile_picture_placeholder)
            .target(
                onSuccess = { result ->
                    binding.toolbar.logo = result.asDrawable(resources)
                    binding.toolbar.isLogoAdjustViewBounds
                    setToolbarLogoClickListener()
                }
            )
            .build()
        imageLoader.enqueue(request)

        binding.ivCover.load(user?.coverImage?.originalOrDown(), imageLoader = imageLoader) {
            val placeholderDrawable =
                SurfaceColors.SURFACE_0.getColor(requireContext()).toDrawable()
            placeholder(placeholderDrawable)
            error(placeholderDrawable)
            fallback(placeholderDrawable)
        }
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
