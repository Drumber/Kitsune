package io.github.drumber.kitsune.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.feed.compose.FeedListScreen
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedListFragment : Fragment(), NavigationBarView.OnItemReselectedListener {

    private val viewModel: FeedListViewModel by viewModel()

    private val feedType: FeedType
        get() = FeedType.valueOf(arguments?.getString(ARG_FEED_TYPE) ?: FeedType.GLOBAL.name)

    private val userId: String? get() = arguments?.getString(ARG_USER_ID)

    private val groupId: String? get() = arguments?.getString(ARG_GROUP_ID)

    private val hostDestId: Int
        get() = arguments?.getInt(ARG_HOST_DEST_ID, R.id.feed_fragment) ?: R.id.feed_fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { Content() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (feedType) {
            FeedType.USER -> userId?.let { viewModel.setUserFeed(it) }
            FeedType.GROUP -> groupId?.let { viewModel.setGroupFeed(it) }
            else -> viewModel.setFeedType(feedType)
        }
    }

    @Composable
    private fun Content() {
        val posts = viewModel.dataSource.collectAsLazyPagingItems()
        val pinnedPost by viewModel.pinnedPost.collectAsStateWithLifecycle()
        val loginRequired by viewModel.loginRequired.collectAsStateWithLifecycle(initialValue = false)
        val interactionStates by viewModel.interactionStates.collectAsStateWithLifecycle(initialValue = emptyMap())
        val revealedPosts by viewModel.revealedPosts.collectAsStateWithLifecycle(initialValue = emptySet())
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            viewModel.likeEvents.collect { event ->
                snackbarMessage = when (event) {
                    FeedListViewModel.LikeEvent.LoginRequired ->
                        getString(R.string.comment_login_required)
                    is FeedListViewModel.LikeEvent.Failed ->
                        getString(R.string.comment_action_failed)
                    is FeedListViewModel.LikeEvent.Updated -> null
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.actionEvents.collect { event ->
                when (event) {
                    FeedListViewModel.ActionEvent.PostDeleted -> {
                        posts.refresh()
                        viewModel.reloadPinnedPost()
                        snackbarMessage = getString(R.string.post_deleted)
                    }
                    FeedListViewModel.ActionEvent.Error ->
                        snackbarMessage = getString(R.string.comment_action_failed)
                }
            }
        }

        FeedListScreen(
            posts = posts,
            pinnedPost = pinnedPost,
            loginRequired = loginRequired,
            interactionStates = interactionStates,
            revealedPosts = revealedPosts,
            nsfwAllowed = viewModel.nsfwAllowed,
            currentUserId = viewModel.currentUserId(),
            snackbarMessage = snackbarMessage,
            onSnackbarShown = { snackbarMessage = null },
            onRefresh = { posts.refresh(); viewModel.reloadPinnedPost() },
            onPostClick = { post -> navigateToPostDetail(post) },
            onLikeClick = { post, targetLiked -> viewModel.togglePostLike(post, targetLiked) },
            onRevealClick = { post -> viewModel.revealPost(post) },
            onMediaClick = { post -> navigateToMedia(post) },
            onEditClick = { post -> navigateToEditPost(post) },
            onDeleteClick = { post -> viewModel.deletePost(post) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) }
        )
    }

    private fun navigateToPostDetail(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(hostDestId, action)
    }

    private fun navigateToEditPost(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        findNavController().navigateSafe(hostDestId, action)
    }

    private fun navigateToMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(hostDestId, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(hostDestId, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {}

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

        fun newUserFeedInstance(userId: String, hostDestId: Int) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, FeedType.USER.name)
                putString(ARG_USER_ID, userId)
                putInt(ARG_HOST_DEST_ID, hostDestId)
            }
        }

        fun newGroupFeedInstance(groupId: String, hostDestId: Int) = FeedListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEED_TYPE, FeedType.GROUP.name)
                putString(ARG_GROUP_ID, groupId)
                putInt(ARG_HOST_DEST_ID, hostDestId)
            }
        }
    }
}
