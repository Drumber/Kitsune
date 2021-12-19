package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayoutMediator
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentMainBinding
import io.github.drumber.kitsune.util.extensions.recyclerView
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener

class MainFragment : Fragment(R.layout.fragment_main), NavigationBarView.OnItemReselectedListener {

    private val binding: FragmentMainBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExploreViewPager()

        binding.tabLayoutExplore.initMarginWindowInsetsListener(
            left = true,
            top = true,
            right = true,
            consume = false
        )
        binding.nsvContent.initMarginWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )
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