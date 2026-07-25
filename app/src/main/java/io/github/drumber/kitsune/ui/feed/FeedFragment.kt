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
import io.github.drumber.kitsune.ui.feed.compose.FeedScreen
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.compose.koinViewModel

class FeedFragment : Fragment(), NavigationBarView.OnItemReselectedListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { Content() }

    override fun onNavigationItemReselected(item: MenuItem) = Unit

    @Composable
    @Suppress("LongMethod")
    private fun Content() {
        val globalVm: FeedListViewModel = koinViewModel(key = "feed_global")
        val followingVm: FeedListViewModel = koinViewModel(key = "feed_following")

        LaunchedEffect(Unit) {
            globalVm.setFeedType(FeedType.GLOBAL)
            followingVm.setFeedType(FeedType.FOLLOWING)
        }

        val globalPosts = globalVm.dataSource.collectAsLazyPagingItems()
        val followingPosts = followingVm.dataSource.collectAsLazyPagingItems()
        val interactionStates by globalVm.interactionStates.collectAsStateWithLifecycle(initialValue = emptyMap())
        val revealedPosts by globalVm.revealedPosts.collectAsStateWithLifecycle(initialValue = emptySet())
        val loginRequired by followingVm.loginRequired.collectAsStateWithLifecycle(initialValue = false)
        var globalSnackbar by remember { mutableStateOf<String?>(null) }
        var followingSnackbar by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            globalVm.likeEvents.collect { event ->
                globalSnackbar = snackbarTextForLikeEvent(event)
            }
        }
        LaunchedEffect(Unit) {
            globalVm.actionEvents.collect { event ->
                globalSnackbar = when (event) {
                    FeedListViewModel.ActionEvent.PostDeleted -> {
                        globalPosts.refresh()
                        getString(R.string.post_deleted)
                    }
                    FeedListViewModel.ActionEvent.Error -> getString(R.string.comment_action_failed)
                }
            }
        }
        LaunchedEffect(Unit) {
            followingVm.likeEvents.collect { event ->
                followingSnackbar = snackbarTextForLikeEvent(event)
            }
        }
        LaunchedEffect(Unit) {
            followingVm.actionEvents.collect { event ->
                followingSnackbar = when (event) {
                    FeedListViewModel.ActionEvent.PostDeleted -> {
                        followingPosts.refresh()
                        getString(R.string.post_deleted)
                    }
                    FeedListViewModel.ActionEvent.Error -> getString(R.string.comment_action_failed)
                }
            }
        }

        FeedScreen(
            globalPosts = globalPosts,
            followingPosts = followingPosts,
            interactionStates = interactionStates,
            revealedPosts = revealedPosts,
            loginRequired = loginRequired,
            nsfwAllowed = globalVm.nsfwAllowed,
            currentUserId = globalVm.currentUserId(),
            globalSnackbarMessage = globalSnackbar,
            followingSnackbarMessage = followingSnackbar,
            onGlobalSnackbarShown = { globalSnackbar = null },
            onFollowingSnackbarShown = { followingSnackbar = null },
            onNavigateToGroups = {
                val action = FeedFragmentDirections.actionGlobalGroupsFragment()
                findNavController().navigateSafe(R.id.feed_fragment, action)
            },
            onNavigateToNotifications = {
                val action = FeedFragmentDirections.actionGlobalNotificationsFragment()
                findNavController().navigateSafe(R.id.feed_fragment, action)
            },
            onCreatePost = {
                val action = FeedFragmentDirections.actionGlobalCreatePostFragment()
                findNavController().navigateSafe(R.id.feed_fragment, action)
            },
            onPostClick = { post -> navigateToPostDetail(post) },
            onLikeClick = { post, targetLiked, page ->
                if (page == 0) {
                    globalVm.togglePostLike(post, targetLiked)
                } else {
                    followingVm.togglePostLike(post, targetLiked)
                }
            },
            onRevealClick = { post, page ->
                if (page == 0) globalVm.revealPost(post) else followingVm.revealPost(post)
            },
            onMediaClick = { post -> navigateToMedia(post) },
            onEditClick = { post -> navigateToEditPost(post) },
            onDeleteClick = { post, page ->
                if (page == 0) globalVm.deletePost(post) else followingVm.deletePost(post)
            },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onRefresh = { page ->
                if (page == 0) globalPosts.refresh() else followingPosts.refresh()
            }
        )
    }

    private fun snackbarTextForLikeEvent(event: FeedListViewModel.LikeEvent): String? = when (event) {
        FeedListViewModel.LikeEvent.LoginRequired -> getString(R.string.comment_login_required)
        is FeedListViewModel.LikeEvent.Failed -> getString(R.string.comment_action_failed)
        is FeedListViewModel.LikeEvent.Updated -> null
    }

    private fun navigateToPostDetail(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(R.id.feed_fragment, action)
    }

    private fun navigateToEditPost(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        findNavController().navigateSafe(R.id.feed_fragment, action)
    }

    private fun navigateToMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.feed_fragment, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.feed_fragment, action)
    }
}
