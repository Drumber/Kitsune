package io.github.drumber.kitsune.ui.feed

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FeedViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POS_GLOBAL -> FeedListFragment.newInstance(FeedType.GLOBAL)
            POS_FOLLOWING -> FeedListFragment.newInstance(FeedType.FOLLOWING)
            else -> throw IllegalStateException("Invalid position '$position'. There are only 2 fragments!")
        }
    }

    companion object {
        const val POS_GLOBAL = 0
        const val POS_FOLLOWING = 1
    }
}
