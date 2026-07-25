package io.github.drumber.kitsune.ui.groupdetail

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.tabs.TabLayout
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.FragmentGroupDetailBinding
import io.github.drumber.kitsune.ui.feed.FeedListFragment
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GroupDetailFragment : Fragment(R.layout.fragment_group_detail) {

    private val args: GroupDetailFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentGroupDetailBinding::bind)

    private val viewModel: GroupDetailViewModel by viewModel {
        parametersOf(args.groupId)
    }

    private var isPostsTab = false
    private var canPost = false
    private var hasSelectedDefaultTab = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Don't override the tab the user already had selected before a config change.
        if (savedInstanceState != null) {
            hasSelectedDefaultTab = true
        }

        binding.collapsingToolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.nestedScrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        binding.btnJoin.setOnClickListener {
            viewModel.toggleMembership()
        }

        binding.ivCover.setOnClickListener {
            viewModel.group.value?.let { group ->
                val groupName = group.name
                group.coverImageUrl?.let { imageUrl ->
                    openPhotoViewActivity(imageUrl, groupName, sharedElement = binding.ivCover)
                }
            }
        }

        initGroupFeed()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { loading ->
                    binding.progressBar.isVisible = loading && viewModel.group.value == null
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.group.collectLatest { group ->
                    group?.let { bindGroup(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.membershipState.collectLatest { state ->
                    bindMembershipState(state)
                    canPost = state.isVisible
                    updateFabVisibility()
                    if (!hasSelectedDefaultTab && state.isMember) {
                        hasSelectedDefaultTab = true
                        binding.tabLayoutGroup.getTabAt(TAB_POSTS)?.select()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        GroupDetailViewModel.Event.LoginRequired ->
                            showSnackbar(binding.root, R.string.group_login_required)

                        GroupDetailViewModel.Event.JoinFailed ->
                            showSnackbar(binding.root, R.string.group_join_failed)

                        GroupDetailViewModel.Event.LeaveFailed ->
                            showSnackbar(binding.root, R.string.group_leave_failed)
                    }
                }
            }
        }
    }

    private fun initGroupFeed() {
        binding.tabLayoutGroup.isVisible = true

        if (childFragmentManager.findFragmentById(R.id.feed_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.feed_container,
                    FeedListFragment.newGroupFeedInstance(args.groupId, R.id.group_detail_fragment)
                )
                .commit()
        }

        binding.tabLayoutGroup.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isPostsTab = tab.position == TAB_POSTS
                binding.nestedScrollView.isVisible = !isPostsTab
                binding.feedContainer.isVisible = isPostsTab
                updateFabVisibility()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.fabPost.setOnClickListener {
            val action = GroupDetailFragmentDirections.actionGlobalCreatePostFragment(
                targetGroupId = args.groupId,
                targetGroupName = viewModel.group.value?.name
            )
            findNavController().navigateSafe(R.id.group_detail_fragment, action)
        }
    }

    private fun updateFabVisibility() {
        binding.fabPost.isVisible = isPostsTab && canPost
    }

    private fun bindMembershipState(state: GroupDetailViewModel.MembershipState) {
        binding.btnJoin.apply {
            isVisible = state.isVisible
            isEnabled = !state.isLoading
            setText(
                if (state.isMember) {
                    R.string.group_action_leave
                } else {
                    R.string.group_action_join
                }
            )
        }
    }

    private fun bindGroup(group: Group) {
        val glide = Glide.with(this)

        glide.load(group.coverImageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.cover_placeholder)
            .into(binding.ivCover)

        glide.load(group.avatarUrl)
            .placeholder(R.drawable.ic_group_24)
            .into(binding.ivAvatar)

        binding.toolbar.title = group.name
        binding.tvName.text = group.name

        binding.tvTagline.apply {
            val tagline = group.tagline?.takeUnless { it.isBlank() }
            isVisible = tagline != null
            text = tagline
        }

        binding.tvMembersCount.text = resources.getQuantityString(
            R.plurals.group_members_count,
            group.membersCount,
            group.membersCount
        )

        binding.chipCategory.apply {
            val name = group.categoryName?.takeUnless { it.isBlank() }
            isVisible = name != null
            text = name
        }

        bindSection(
            header = binding.tvAboutHeader,
            content = binding.tvAbout,
            text = group.about
        )
        bindSection(
            header = binding.tvRulesHeader,
            content = binding.tvRules,
            text = group.rules
        )
    }

    private fun bindSection(header: View, content: android.widget.TextView, text: String?) {
        val value = text?.takeUnless { it.isBlank() }
        header.isVisible = value != null
        content.isVisible = value != null
        content.text = value
    }

    companion object {
        private const val TAB_POSTS = 1
    }
}
