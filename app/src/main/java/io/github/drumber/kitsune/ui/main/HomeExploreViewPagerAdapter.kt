package io.github.drumber.kitsune.ui.main

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.drumber.kitsune.data.common.model.media.MediaType

class HomeExploreViewPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeExploreFragment().apply {
                arguments = bundleOf(HomeExploreFragment.BUNDLE_MEDIA_TYPE to MediaType.Anime)
            }
            1 -> HomeExploreFragment().apply {
                arguments = bundleOf(HomeExploreFragment.BUNDLE_MEDIA_TYPE to MediaType.Manga)
            }
            else -> throw IllegalStateException("Invalid position '$position'. There are ony 2 fragments!")
        }
    }
}