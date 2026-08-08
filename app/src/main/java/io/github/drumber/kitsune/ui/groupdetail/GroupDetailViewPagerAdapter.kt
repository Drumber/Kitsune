package io.github.drumber.kitsune.ui.groupdetail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.feed.FeedListFragment

class GroupDetailViewPagerAdapter(
    private val groupId: String,
    fragment: Fragment,
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            POS_ABOUT -> GroupDetailAboutFragment.newInstance()
            POS_FEED -> FeedListFragment.newGroupFeedInstance(groupId, R.id.group_detail_fragment)
            else -> throw IllegalStateException("Invalid position '$position'. There are only 2 fragments for groups!")
        }
    }

    companion object {
        const val POS_ABOUT = 0
        const val POS_FEED = 1
    }
}