package io.github.drumber.kitsune.ui.notifications

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.databinding.FragmentNotificationsBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.NotificationPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.reactiondetail.ReactionDetailFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : Fragment(R.layout.fragment_notifications),
    OnItemClickListener<Notification> {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationsBinding.bind(view)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val adapter = NotificationPagingAdapter(
            glide = Glide.with(this),
            listener = this
        )
        binding.rvNotifications.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvNotifications.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener { adapter.refresh() }
        }

        val loginRequired = viewModel.loginRequired
        binding.tvLoginRequired.isVisible = loginRequired
        if (loginRequired) {
            binding.rvNotifications.isVisible = false
            binding.layoutLoading.root.isVisible = false
            binding.swipeRefreshLayout.isEnabled = false
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvNotifications,
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
                viewModel.notifications.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }
    }

    override fun onItemClick(view: View, item: Notification) {
        val reactionId = item.targetReactionId
        if (reactionId != null) {
            val action = ReactionDetailFragmentDirections
                .actionGlobalReactionDetailFragment(reactionId)
            findNavController().navigateSafe(R.id.notifications_fragment, action)
            return
        }
        val post = item.targetPost ?: return
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(R.id.notifications_fragment, action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvNotifications.adapter = null
        _binding = null
    }
}
