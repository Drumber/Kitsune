package io.github.drumber.kitsune.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.ui.profile.about.MyProfileAboutFragment
import io.github.drumber.kitsune.ui.profile.about.UserProfileAboutFragment

class ProfileViewPagerAdapter(
    private val userId: String,
    private val isMyProfile: Boolean,
    private val hostDestId: Int,
    fragment: Fragment
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> if (isMyProfile) MyProfileAboutFragment() else UserProfileAboutFragment()
            1 -> FeedListFragment.newUserFeedInstance(userId, hostDestId)
            else -> throw IllegalStateException("Invalid position '$position'. There are ony 2 fragments!")
        }
    }
}
