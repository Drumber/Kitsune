package io.github.drumber.kitsune.ui.groupdetail

import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.databinding.FragmentGroupDetailAboutBinding
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupDetailAboutFragment : Fragment(R.layout.fragment_group_detail_about),
    NavigationBarView.OnItemReselectedListener {

    private val binding by viewBinding(FragmentGroupDetailAboutBinding::bind)

    private val viewModel: GroupDetailViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nestedScrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        binding.btnJoin.setOnClickListener {
            viewModel.toggleMembership()
        }

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

        glide.load(group.avatarUrl)
            .placeholder(R.drawable.ic_group_24)
            .into(binding.ivAvatar)

        binding.tvName.text = group.name

        binding.tvTagline.apply {
            val tagline = group.tagline?.takeUnless { it.isBlank() }
            isVisible = tagline != null
            text = tagline
        }

        binding.tvMembersCount.text = resources.getQuantityString(
            R.plurals.group_members_count,
            group.membersCount,
            NumberFormat.getNumberInstance().format(group.membersCount)
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

    private fun bindSection(header: View, content: TextView, text: String?) {
        val value = text?.takeUnless { it.isBlank() }
        header.isVisible = value != null
        content.isVisible = value != null
        content.text = value
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        binding.nestedScrollView.smoothScrollOrJumpToTop()
    }

    companion object {
        fun newInstance() = GroupDetailAboutFragment()
    }
}
