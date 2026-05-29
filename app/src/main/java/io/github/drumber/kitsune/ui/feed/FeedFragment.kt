package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentFeedBinding
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener

class FeedFragment : Fragment(R.layout.fragment_feed),
    NavigationBarView.OnItemReselectedListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.tabLayoutFeed.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        binding.viewPagerFeed.adapter = FeedViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayoutFeed, binding.viewPagerFeed) { tab, position ->
            tab.text = when (position) {
                FeedViewPagerAdapter.POS_GLOBAL -> getString(R.string.feed_tab_global)
                FeedViewPagerAdapter.POS_FOLLOWING -> getString(R.string.feed_tab_following)
                else -> null
            }
        }.attach()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
