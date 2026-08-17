package io.github.drumber.kitsune.ui.groupdetail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil3.ImageLoader
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.FragmentGroupDetailBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class GroupDetailFragment : Fragment(R.layout.fragment_group_detail),
    NavigationBarView.OnItemReselectedListener {

    private val args: GroupDetailFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentGroupDetailBinding::bind)

    private val viewModel: GroupDetailViewModel by viewModel {
        parametersOf(args.groupId)
    }

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())

    private var hasSelectedDefaultTab = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Don't override the tab the user already had selected before a config change.
        if (savedInstanceState != null) {
            hasSelectedDefaultTab = true
        }

        binding.collapsingToolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.tabLayoutGroup.initPaddingWindowInsetsListener(left = true, right = true, consume = false)

        binding.ivCover.setOnClickListener {
            viewModel.group.value?.let { group ->
                val groupName = group.name
                group.coverImageUrl?.let { imageUrl ->
                    openPhotoViewActivity(
                        imageUrl,
                        groupName,
                        sharedElement = binding.ivCover,
                        useSocialImageLoader = true
                    )
                }
            }
        }

        initGroupFeed()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.group.collectLatest { group ->
                    group?.let { bindGroup(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.membershipState.collectLatest { state ->
                    if (!hasSelectedDefaultTab && state.isMember) {
                        hasSelectedDefaultTab = true
                        binding.tabLayoutGroup.getTabAt(GroupDetailViewPagerAdapter.POS_FEED)?.select()
                    }
                }
            }
        }
    }

    private fun initGroupFeed() {
        binding.viewPagerGroup.adapter = GroupDetailViewPagerAdapter(args.groupId, this)

        TabLayoutMediator(binding.tabLayoutGroup, binding.viewPagerGroup) { tab, position ->
            when (position) {
                GroupDetailViewPagerAdapter.POS_ABOUT -> tab.setText(R.string.group_tab_about)
                GroupDetailViewPagerAdapter.POS_FEED -> tab.setText(R.string.group_tab_posts)
            }
        }.attach()

        binding.tabLayoutGroup.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val isPostsTab = tab.position == GroupDetailViewPagerAdapter.POS_FEED
                updateFabVisibility(isPostsTab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        binding.fabPost.setOnClickListener {
            val action = GroupDetailFragmentDirections.actionGlobalCreatePostFragment(
                targetGroupId = args.groupId,
                targetGroupName = viewModel.group.value?.name
            )
            findNavController().navigateSafe(R.id.group_detail_fragment, action)
        }
    }

    private fun updateFabVisibility(visible: Boolean) {
        if (visible) {
            binding.fabPost.show()
        } else {
            binding.fabPost.hide()
        }
    }

    private fun bindGroup(group: Group) {
        binding.ivCover.load(group.coverImageUrl, imageLoader = imageLoader) {
            placeholder(R.drawable.cover_placeholder)
            error(R.drawable.cover_placeholder)
            fallback(R.drawable.cover_placeholder)
        }

        binding.toolbar.title = group.name
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        val currentChild = childFragmentManager
            .findFragmentByTag("f" + binding.viewPagerGroup.currentItem)
        if (currentChild is NavigationBarView.OnItemReselectedListener) {
            currentChild.onNavigationItemReselected(item)
        }
    }
}
