package io.github.drumber.kitsune.ui.profile.follow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.databinding.FragmentFollowListBinding
import io.github.drumber.kitsune.ui.adapter.paging.FollowUserPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FollowListFragment : Fragment(R.layout.fragment_follow_list) {

    private val binding by viewBinding(FragmentFollowListBinding::bind)

    private val args: FollowListFragmentArgs by navArgs()

    private val viewModel: FollowListViewModel by viewModel {
        parametersOf(args.userId, args.listType)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.title = buildTitle()

        val adapter = FollowUserPagingAdapter(
            glide = Glide.with(this),
            onUserClick = ::navigateToUser,
            onFollowClick = { userId -> viewModel.toggleFollow(userId) },
            onBindUser = { userId -> viewModel.resolveFollowState(userId) },
            showButtonFor = { userId -> viewModel.showButtonFor(userId) }
        )
        binding.rvFollowUsers.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvFollowUsers.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener { adapter.refresh() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvFollowUsers,
                        adapter.itemCount,
                        loadState
                    )
                    binding.swipeRefreshLayout.isRefreshing =
                        loadState.refresh is LoadState.Loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.followStates.collectLatest { states ->
                    states.forEach { (userId, state) ->
                        adapter.setFollowState(userId, state)
                    }
                }
            }
        }
    }

    private fun buildTitle(): String {
        val titleRes = when (args.listType) {
            FollowListType.FOLLOWING -> R.string.follow_list_following_title
            FollowListType.FOLLOWERS -> R.string.follow_list_followers_title
        }
        return getString(titleRes)
    }

    private fun navigateToUser(userId: String) {
        val action = UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId, null)
        findNavController().navigateSafe(R.id.follow_list_fragment, action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFollowUsers.adapter = null
    }
}
