package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.FragmentFeedListBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.adapter.paging.PinnedPostAdapter
import io.github.drumber.kitsune.ui.adapter.paging.PostInteractionListener
import io.github.drumber.kitsune.ui.adapter.paging.PostPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.report.ReportBottomSheet
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.markwon.PostContentRenderer
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class FeedListFragment : Fragment(R.layout.fragment_feed_list), PostInteractionListener,
    NavigationBarView.OnItemReselectedListener {

    private val binding by viewBinding(FragmentFeedListBinding::bind)

    private var feedAdapter: PostPagingAdapter? = null

    /** Header adapter showing the profile's pinned post; used only for [FeedType.USER] feeds. */
    private var pinnedPostAdapter: PinnedPostAdapter? = null

    private val viewModel: FeedListViewModel by viewModel()

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())
    private val contentRenderer: PostContentRenderer by inject()

    private var isLoginRequired = false

    private val feedType: FeedType
        get() = FeedType.valueOf(
            arguments?.getString(ARG_FEED_TYPE) ?: FeedType.GLOBAL.name
        )

    private val userId: String?
        get() = arguments?.getString(ARG_USER_ID)

    private val groupId: String?
        get() = arguments?.getString(ARG_GROUP_ID)

    /** Navigation graph destination id this fragment is hosted in, used by [navigateSafe]. */
    private val hostDestId: Int
        get() = arguments?.getInt(ARG_HOST_DEST_ID, R.id.feed_fragment) ?: R.id.feed_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (feedType) {
            FeedType.USER -> userId?.let { viewModel.setUserFeed(it) }
            FeedType.GROUP -> groupId?.let { viewModel.setGroupFeed(it) }
            else -> viewModel.setFeedType(feedType)
        }

        val adapter = PostPagingAdapter(
            imageLoader = imageLoader,
            contentRenderer = contentRenderer,
            nsfwAllowed = viewModel.nsfwAllowed,
            currentUserId = viewModel.currentUserId(),
            listener = this
        )
        feedAdapter = adapter

        val feedWithFooter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvFeed.adapter = if (feedType == FeedType.USER) {
            val pinnedAdapter = PinnedPostAdapter(
                imageLoader = imageLoader,
                contentRenderer = contentRenderer,
                nsfwAllowed = viewModel.nsfwAllowed,
                currentUserId = viewModel.currentUserId(),
                listener = this
            )
            pinnedPostAdapter = pinnedAdapter
            ConcatAdapter(pinnedAdapter, feedWithFooter)
        } else {
            feedWithFooter
        }
        binding.rvFeed.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvFeed.itemAnimator = null

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener {
                adapter.refresh()
                viewModel.reloadPinnedPost()
            }
            initPaddingWindowInsetsListener(
                left = true,
                right = true,
                consume = false
            )
        }

        binding.rvFeed.initPaddingWindowInsetsListener(bottom = true, consume = false)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    if (isLoginRequired) return@collectLatest
                    // Count the pinned post as content so the empty state does not hide it when
                    // the paginated feed itself is empty.
                    val itemCount = adapter.itemCount + (pinnedPostAdapter?.itemCount ?: 0)
                    binding.layoutLoading.updateLoadState(
                        binding.rvFeed,
                        itemCount,
                        loadState
                    )
                    binding.swipeRefreshLayout.isRefreshing =
                        loadState.refresh is LoadState.Loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginRequired.collectLatest { loginRequired ->
                    isLoginRequired = loginRequired
                    binding.tvLoginRequired.isVisible = loginRequired
                    if (loginRequired) {
                        binding.rvFeed.isVisible = false
                        binding.layoutLoading.root.isVisible = false
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
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
                viewModel.pinnedPost.collectLatest { post ->
                    pinnedPostAdapter?.setPost(post)
                    // If the paginated feed is empty, its empty state hides the list; make sure a
                    // pinned post stays visible in that case.
                    if (post != null && !binding.layoutLoading.progressBar.isVisible) {
                        binding.rvFeed.isVisible = true
                        binding.layoutLoading.root.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.interactionStates.collectLatest { states ->
                    states.forEach { (postId, state) ->
                        adapter.applyInteraction(
                            postId,
                            state.isLiked,
                            state.likesCount,
                            state.commentsCount,
                            state.likerAvatars
                        )
                        pinnedPostAdapter?.applyInteraction(
                            postId,
                            state.isLiked,
                            state.likesCount,
                            state.commentsCount,
                            state.likerAvatars
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.revealedPosts.collectLatest { ids ->
                    ids.forEach {
                        adapter.markRevealed(it)
                        pinnedPostAdapter?.markRevealed(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.likeEvents.collect { event ->
                    when (event) {
                        FeedListViewModel.LikeEvent.LoginRequired ->
                            showSnackbar(binding.root, R.string.comment_login_required)

                        is FeedListViewModel.LikeEvent.Failed -> {
                            adapter.setLikeState(event.postId, event.isLiked, event.count)
                            pinnedPostAdapter?.setLikeState(
                                event.postId,
                                event.isLiked,
                                event.count
                            )
                            showSnackbar(binding.root, R.string.comment_action_failed)
                        }

                        is FeedListViewModel.LikeEvent.Updated -> {
                            adapter.setLikeState(event.postId, event.isLiked, event.count)
                            pinnedPostAdapter?.setLikeState(
                                event.postId,
                                event.isLiked,
                                event.count
                            )
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.actionEvents.collectLatest { event ->
                    when (event) {
                        FeedListViewModel.ActionEvent.PostDeleted -> {
                            adapter.refresh()
                            viewModel.reloadPinnedPost()
                            showSnackbar(binding.root, R.string.post_deleted)
                        }

                        FeedListViewModel.ActionEvent.Error ->
                            showSnackbar(binding.root, R.string.comment_action_failed)
                    }
                }
            }
        }
    }

    private fun navigateToEditPost(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        findNavController().navigateSafe(hostDestId, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(hostDestId, action)
    }

    private fun confirmDeletePost(post: Post) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_post_confirm_title)
            .setMessage(R.string.delete_post_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deletePost(post) }
            .show()
    }

    override fun onPostClick(view: View, post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(hostDestId, action)
    }

    override fun onLikeClick(post: Post, targetLiked: Boolean) {
        viewModel.togglePostLike(post, targetLiked)
    }

    override fun onRevealClick(post: Post) {
        viewModel.revealPost(post)
    }

    override fun onMediaClick(post: Post) {
        openMedia(post)
    }

    override fun onShareClick(post: Post) {
        startUrlShareIntent("${Kitsu.BASE_URL}/posts/${post.id}")
    }

    override fun onEditClick(post: Post) {
        navigateToEditPost(post)
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
        navigateToUserProfile(userId)
    }

    private fun openMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(hostDestId, action)
    }

    override fun onNavigationItemReselected(p0: MenuItem) {
        if (view == null) return
        binding.rvFeed.smoothScrollOrJumpToTop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        feedAdapter = null
        pinnedPostAdapter = null
        binding.rvFeed.adapter = null
    }

    companion object {
        const val ARG_FEED_TYPE = "feed_type"
        const val ARG_USER_ID = "user_id"
        const val ARG_GROUP_ID = "group_id"
        const val ARG_HOST_DEST_ID = "host_dest_id"

        fun newInstance(feedType: FeedType) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, feedType.name)
            }
        }

        /** Creates a fragment showing the profile feed of [userId], hosted in [hostDestId]. */
        fun newUserFeedInstance(userId: String, hostDestId: Int) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, FeedType.USER.name)
                putString(ARG_USER_ID, userId)
                putInt(ARG_HOST_DEST_ID, hostDestId)
            }
        }

        /** Creates a fragment showing the feed of [groupId], hosted in [hostDestId]. */
        fun newGroupFeedInstance(groupId: String, hostDestId: Int) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, FeedType.GROUP.name)
                putString(ARG_GROUP_ID, groupId)
                putInt(ARG_HOST_DEST_ID, hostDestId)
            }
        }
    }
}
