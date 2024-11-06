package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.ui.main.MainFragmentViewModel.NavigationAction
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel

class MainFragment : Fragment(R.layout.fragment_main), NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentMainBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by koinNavGraphViewModel(R.id.main_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        initExploreViewPager()

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
        binding.tabLayoutExplore.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
        binding.swipeRefreshLayout.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
        binding.nsvContent.initPaddingWindowInsetsListener(bottom = true, consume = false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reloadFinished.collect { isReloadFinished ->
                    binding.swipeRefreshLayout.isRefreshing = !isReloadFinished
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationAction.collect(::handleNavigationAction)
            }
        }
    }

    private fun handleNavigationAction(navigationAction: NavigationAction) {
        when (navigationAction) {
            is NavigationAction.OpenMediaList -> {
                val action =
                    MainFragmentDirections.actionMainFragmentToMediaListFragment(navigationAction.mediaSelector, navigationAction.title)
                findNavController().navigateSafe(R.id.main_fragment, action)
            }

            is NavigationAction.OpenMediaDetails -> {
                val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(navigationAction.mediaDto)
                val detailsTransitionName = getString(R.string.details_poster_transition_name)
                val extras = FragmentNavigatorExtras(navigationAction.sharedElement to detailsTransitionName)
                findNavController().navigateSafe(R.id.main_fragment, action, extras)
            }
        }
    }

    private fun initExploreViewPager() {
        binding.viewPagerExplore.apply {
            adapter = HomeExploreViewPagerAdapter(this@MainFragment)
            recyclerView.isNestedScrollingEnabled = false
        }

        TabLayoutMediator(binding.tabLayoutExplore, binding.viewPagerExplore) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.anime)
                }

                1 -> {
                    tab.text = getString(R.string.manga)
                }
            }
        }.attach()

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            isRefreshing = isRefreshing && viewModel.isSomeEntryReloading()

            setOnRefreshListener {
                when (binding.viewPagerExplore.currentItem) {
                    0 -> viewModel.refreshAnimeData()
                    1 -> viewModel.refreshMangaData()
                }
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.nsvContent.smoothScrollTo(0, 0)
        binding.appBarLayout.setExpanded(true)
    }

    override fun onDestroyView() {
        binding.viewPagerExplore.adapter = null
        super.onDestroyView()
    }

}