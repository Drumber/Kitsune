package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentFeedBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedFragment : Fragment(R.layout.fragment_feed),
    NavigationBarView.OnItemReselectedListener,
    FeedListFragment.OnNavigationListener {

    companion object {
        private const val KEY_LAST_NAV_DESTINATION = "last_nav_destination"
    }

    private val binding by viewBinding(FragmentFeedBinding::bind)

    private val viewModel: FeedViewModel by viewModel()

    private var lastNavDestination: Int? = null
    private lateinit var notificationsBadge: BadgeDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(KEY_LAST_NAV_DESTINATION) == true) {
            lastNavDestination = savedInstanceState.getInt(KEY_LAST_NAV_DESTINATION)
        }

        lastNavDestination?.let { applyTransitions(it) }
        notificationsBadge = BadgeDrawable.create(requireContext())
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.tabLayoutFeed.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_groups -> {
                    applyTransitions(R.id.groups_fragment)
                    findNavController().navigateSafe(
                        R.id.feed_fragment,
                        FeedFragmentDirections.actionGlobalGroupsFragment()
                    )
                    true
                }

                R.id.menu_notifications -> {
                    applyTransitions(R.id.notifications_fragment)
                    findNavController().navigateSafe(
                        R.id.feed_fragment,
                        FeedFragmentDirections.actionGlobalNotificationsFragment()
                    )
                    true
                }

                else -> false
            }
        }

        BadgeUtils.attachBadgeDrawable(notificationsBadge, binding.toolbar, R.id.menu_notifications)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.unseenNotificationsCount.collectLatest { unseenNotificationsCount ->
                    val count = unseenNotificationsCount ?: 0
                    notificationsBadge.apply {
                        isVisible = count > 0
                        number = count
                    }
                }
            }
        }

        binding.fabCreatePost.initMarginWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )
        binding.fabCreatePost.setOnClickListener {
            val extras = FragmentNavigatorExtras(it to getString(R.string.create_post_transition_name))
            applyTransitions(R.id.create_post_fragment)
            findNavController().navigateSafe(
                R.id.feed_fragment,
                FeedFragmentDirections.actionGlobalCreatePostFragment(),
                extras
            )
        }

        val feedViewPagerAdapter = FeedViewPagerAdapter(this)
        binding.viewPagerFeed.adapter = feedViewPagerAdapter

        TabLayoutMediator(binding.tabLayoutFeed, binding.viewPagerFeed) { tab, position ->
            tab.text = when (position) {
                FeedViewPagerAdapter.POS_GLOBAL -> getString(R.string.feed_tab_global)
                FeedViewPagerAdapter.POS_FOLLOWING -> getString(R.string.feed_tab_following)
                else -> null
            }
        }.attach()

        // restore previously selected feed tab
        if (savedInstanceState == null) {
            val lastSelectedTab = KitsunePref.selectedFeedTab.coerceIn(0, feedViewPagerAdapter.itemCount)
            if (lastSelectedTab != binding.viewPagerFeed.currentItem) {
                binding.viewPagerFeed.setCurrentItem(lastSelectedTab, false)
            }
        }

        binding.viewPagerFeed.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (KitsunePref.selectedFeedTab != position) {
                    KitsunePref.selectedFeedTab = position
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lastNavDestination?.let { outState.putInt(KEY_LAST_NAV_DESTINATION, it) }
    }

    private fun applyTransitions(destinationId: Int) {
        lastNavDestination = destinationId
        when (destinationId) {
            R.id.notifications_fragment, R.id.groups_fragment -> {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
            }

            R.id.create_post_fragment -> {
                val duration = resources.getInteger(R.integer.material_motion_duration_short_2).toLong()
                exitTransition = MaterialElevationScale(false).apply {
                    this.duration = duration
                }
                reenterTransition = MaterialElevationScale(true).apply {
                    this.duration = duration
                }
            }

            R.id.post_detail_fragment -> {
                val transition = MaterialFadeThrough().apply {
                    duration = resources.getInteger(R.integer.material_motion_duration_short_2).toLong()
                }
                exitTransition = transition
                reenterTransition = transition
            }

            R.id.user_profile_fragment -> {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            }

            R.id.details_fragment -> {
                exitTransition = MaterialFadeThrough()
                reenterTransition = MaterialFadeThrough()
            }

            else -> {
                exitTransition = null
                reenterTransition = null
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateUnseenNotificationsCount()
    }

    override fun onFeedListNavigationEvent(destinationId: Int) {
        applyTransitions(destinationId)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.appBarLayout.setExpanded(true)
        val currentChild = childFragmentManager
            .findFragmentByTag("f" + binding.viewPagerFeed.currentItem)
        if (currentChild is NavigationBarView.OnItemReselectedListener) {
            currentChild.onNavigationItemReselected(item)
        }
    }
}
