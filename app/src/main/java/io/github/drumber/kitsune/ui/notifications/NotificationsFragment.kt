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
import coil3.ImageLoader
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.databinding.FragmentNotificationsBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.paging.NotificationPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.reactiondetail.ReactionDetailFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class NotificationsFragment : Fragment(R.layout.fragment_notifications),
    OnItemClickListener<Notification> {

    private val binding by viewBinding(FragmentNotificationsBinding::bind)

    private val viewModel: NotificationsViewModel by viewModel()

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)
        binding.toolbar.initPaddingWindowInsetsListener(left = true, right = true, consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val adapter = NotificationPagingAdapter(imageLoader, listener = this)
        binding.rvNotifications.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvNotifications.layoutManager = layoutManager

        binding.rvNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                markVisibleNotificationsAsSeen(adapter, layoutManager)
            }
        })

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

                    if (loadState.source.refresh is LoadState.NotLoading) {
                        binding.rvNotifications.post {
                            markVisibleNotificationsAsSeen(adapter, layoutManager)
                        }
                    }
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
        val post = item.targetPost
        val actorId = item.actorId

        val action = when {
            reactionId != null -> ReactionDetailFragmentDirections.actionGlobalReactionDetailFragment(reactionId)

            post != null -> PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)

            actorId != null -> UserProfileFragmentDirections.actionGlobalUserProfileFragment(actorId, item.actorName)

            else -> return
        }

        if (!item.isRead) {
            viewModel.markNotificationAsRead(item)
        }
        findNavController().navigateSafe(R.id.notifications_fragment, action)
    }

    private fun markVisibleNotificationsAsSeen(
        adapter: NotificationPagingAdapter,
        layoutManager: LinearLayoutManager
    ) {
        val first = layoutManager.findFirstCompletelyVisibleItemPosition()
        val last = layoutManager.findLastCompletelyVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION) return

        val unseenNotifications = (first..last).mapNotNull { position ->
            adapter.peek(position)
        }.filter { !it.isSeen }

        if (unseenNotifications.isNotEmpty()) {
            viewModel.markNotificationsAsSeen(unseenNotifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvNotifications.adapter = null
    }
}
