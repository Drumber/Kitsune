package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentFeedBinding
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding

class FeedFragment : Fragment(R.layout.fragment_feed),
    NavigationBarView.OnItemReselectedListener {

    private val binding by viewBinding(FragmentFeedBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.tabLayoutFeed.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        binding.viewPagerFeed.adapter = FeedViewPagerAdapter(this)

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_groups -> {
                    findNavController().navigateSafe(
                        R.id.feed_fragment,
                        FeedFragmentDirections.actionGlobalGroupsFragment()
                    )
                    true
                }

                R.id.menu_notifications -> {
                    findNavController().navigateSafe(
                        R.id.feed_fragment,
                        FeedFragmentDirections.actionGlobalNotificationsFragment()
                    )
                    true
                }

                else -> false
            }
        }

        binding.fabCreatePost.initMarginWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )
        binding.fabCreatePost.setOnClickListener {
            findNavController().navigateSafe(
                R.id.feed_fragment,
                FeedFragmentDirections.actionGlobalCreatePostFragment()
            )
        }

        TabLayoutMediator(binding.tabLayoutFeed, binding.viewPagerFeed) { tab, position ->
            tab.text = when (position) {
                FeedViewPagerAdapter.POS_GLOBAL -> getString(R.string.feed_tab_global)
                FeedViewPagerAdapter.POS_FOLLOWING -> getString(R.string.feed_tab_following)
                else -> null
            }
        }.attach()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val isAppBarExpanded = binding.appBarLayout.bottom >= binding.appBarLayout.height
        val currentChild = childFragmentManager
            .findFragmentByTag("f" + binding.viewPagerFeed.currentItem) as? FeedListFragment
        currentChild?.scrollToTopOrRefresh(isAppBarExpanded)
        if (!isAppBarExpanded) {
            binding.appBarLayout.setExpanded(true)
        }
    }
}
