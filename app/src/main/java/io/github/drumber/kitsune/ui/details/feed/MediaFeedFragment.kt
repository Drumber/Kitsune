package io.github.drumber.kitsune.ui.details.feed

import android.os.Bundle
import android.view.MenuItem
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.FragmentMediaFeedBinding
import io.github.drumber.kitsune.ui.adapter.paging.PostInteractionListener
import io.github.drumber.kitsune.ui.adapter.paging.PostPagingAdapter
import io.github.drumber.kitsune.ui.report.ReportBottomSheet
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.markwon.PostContentRenderer
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFeedFragment : Fragment(R.layout.fragment_media_feed),
    PostInteractionListener, NavigationBarView.OnItemReselectedListener {

    private val args: MediaFeedFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentMediaFeedBinding::bind)

    private val viewModel: MediaFeedViewModel by viewModel()

    private val contentRenderer: PostContentRenderer by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.mediaId, args.isAnime)

        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(false)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            rvFeed.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
        }

        val adapter = PostPagingAdapter(
            listener = this,
            contentRenderer = contentRenderer,
            nsfwAllowed = viewModel.nsfwAllowed,
            currentUserId = viewModel.localUserId,
        )
        binding.rvFeed.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvFeed.layoutManager =
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
                        binding.rvFeed,
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
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionEvents.collectLatest { event ->
                    when (event) {
                        MediaFeedViewModel.ActionEvent.PostDeleted -> {
                            adapter.refresh()
                            showSnackbar(binding.root, R.string.post_deleted)
                        }

                        MediaFeedViewModel.ActionEvent.Error ->
                            showSnackbar(binding.root, R.string.comment_action_failed)
                    }
                }
            }
        }
    }

    override fun onPostClick(view: View, post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    override fun onLikeClick(post: Post, targetLiked: Boolean) {
        viewModel.togglePostLike(post, targetLiked)
    }

    override fun onRevealClick(post: Post) {
        viewModel.revealPost(post)
    }

    override fun onMediaClick(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    override fun onShareClick(post: Post) {
        startUrlShareIntent("${Kitsu.BASE_URL}/posts/${post.id}")
    }

    override fun onEditClick(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    override fun onDeleteClick(post: Post) {
        confirmDeletePost(post)
    }

    override fun onReportClick(post: Post) {
        ReportBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ReportBottomSheet.BUNDLE_POST_ID, post.id)
            }
        }.show(childFragmentManager, ReportBottomSheet.TAG)
    }

    override fun onAuthorClick(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.media_feed_fragment, action)
    }

    private fun confirmDeletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_post_confirm_title)
            .setMessage(R.string.delete_post_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deletePost(post) }
            .show()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvFeed.canScrollVertically(-1)) {
            binding.rvFeed.smoothScrollOrJumpToTop()
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }
}
