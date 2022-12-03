package io.github.drumber.kitsune.ui.details.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DetailsTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OverviewTabFragment()
            1 -> EpisodesTabFragment()
            2 -> CharactersTabFragment()
            3 -> FranchiseTabFragment()
            else -> throw IllegalStateException("Invalid details tab position: $position")
        }
    }
}