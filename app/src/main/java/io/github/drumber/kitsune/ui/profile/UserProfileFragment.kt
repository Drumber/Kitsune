package io.github.drumber.kitsune.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.feed.FeedListViewModel
import io.github.drumber.kitsune.ui.feed.compose.FeedListScreen
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.profile.about.ProfileAboutScreen
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/** Read-only profile screen for viewing another user's profile. */
class UserProfileFragment : BaseProfileFragment() {

    private val args: UserProfileFragmentArgs by navArgs()

    override val viewModel: UserProfileViewModel by viewModel {
        parametersOf(args.userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { ProfileContent() }

    @Composable
    private fun ProfileContent() {
        val user by viewModel.userModel.collectAsStateWithLifecycle()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        // Use args.userName as a placeholder while the full profile loads.
        val displayName = user?.name
            ?: user?.slug
            ?: args.userName
            ?: getString(R.string.nav_profile)

        // Show "@slug" as subtitle when there is a distinct display name.
        val subtitle = user?.slug
            ?.takeIf { !user?.name.isNullOrBlank() }
            ?.let { "@$it" }

        ProfileScreen(
            user = user,
            displayName = displayName,
            subtitle = subtitle,
            isMyProfile = false,
            uiState = uiState,
            scrollToTopEvents = scrollToTopEvents,
            aboutTabContent = { scrollState -> AboutTab(user = user, uiState = uiState, scrollState = scrollState) },
            feedTabContent = { scrollState -> FeedTab(userId = user?.id ?: args.userId, scrollState = scrollState) },
            onShareProfile = {
                val profileId = viewModel.getUser()?.slug ?: viewModel.getUser()?.id ?: args.userId
                startUrlShareIntent(Kitsu.USER_URL_PREFIX + profileId)
            },
            onPostOnWall = {
                val u = viewModel.getUser() ?: return@ProfileScreen
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    UserProfileFragmentDirections.actionGlobalCreatePostFragment(
                        targetUserId = u.id,
                        targetUserName = u.name
                    )
                )
            },
            onCoverClick = {
                val url = viewModel.getUser()?.coverImage?.originalOrDown() ?: return@ProfileScreen
                openPhotoViewActivity(url, viewModel.getUser()?.name?.let { "$it Cover" })
            },
            onAvatarClick = {
                val url = viewModel.getUser()?.avatar?.originalOrDown() ?: return@ProfileScreen
                openPhotoViewActivity(url, viewModel.getUser()?.name?.let { "$it Avatar" })
            },
            onNavigateToSettings = {},
            onNavigateToEditProfile = {},
            onLogOut = {},
            onSignIn = {},
            onNavigateUp = { findNavController().navigateUp() }
        )
    }

    @Composable
    private fun AboutTab(
        user: io.github.drumber.kitsune.data.presentation.model.user.User?,
        uiState: ProfileUiState,
        scrollState: LazyListState
    ) {
        // Cast is safe: uiState is always UserProfileUiState for this fragment.
        val userUiState = uiState as? UserProfileUiState
        ProfileAboutScreen(
            user = user,
            isRefreshing = uiState.isRefreshing,
            isInitialLoading = uiState.isInitialLoading,
            followState = userUiState,
            lazyListState = scrollState,
            onRefresh = viewModel::refreshUser,
            onFollowClick = { viewModel.toggleFollow() },
            onFollowingClick = {
                val u = viewModel.getUser() ?: return@ProfileAboutScreen
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    FollowListFragmentDirections.actionGlobalFollowListFragment(
                        u.id, FollowListType.FOLLOWING, u.name
                    )
                )
            },
            onFollowersClick = {
                val u = viewModel.getUser() ?: return@ProfileAboutScreen
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    FollowListFragmentDirections.actionGlobalFollowListFragment(
                        u.id, FollowListType.FOLLOWERS, u.name
                    )
                )
            },
            onWaifuClick = { character -> navigateToCharacterDetails(character) },
            onMediaClick = { media -> navigateToMediaDetails(media) },
            onCharacterClick = { character -> navigateToCharacterDetails(character) },
            onProfileLinkClick = { link -> handleProfileLinkClick(link) }
        )
    }

    @Composable
    private fun FeedTab(userId: String, scrollState: LazyListState) {
        val feedViewModel: FeedListViewModel = koinViewModel(key = "user_profile_feed")
        val context = LocalContext.current

        LaunchedEffect(userId) { feedViewModel.setUserFeed(userId) }

        val posts = feedViewModel.dataSource.collectAsLazyPagingItems()
        val pinnedPost by feedViewModel.pinnedPost.collectAsStateWithLifecycle()
        val loginRequired by feedViewModel.loginRequired.collectAsStateWithLifecycle(false)
        val interactionStates by feedViewModel.interactionStates.collectAsStateWithLifecycle(emptyMap())
        val revealedPosts by feedViewModel.revealedPosts.collectAsStateWithLifecycle(emptySet())
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            feedViewModel.likeEvents.collect { event ->
                snackbarMessage = when (event) {
                    FeedListViewModel.LikeEvent.LoginRequired ->
                        context.getString(R.string.comment_login_required)
                    is FeedListViewModel.LikeEvent.Failed ->
                        context.getString(R.string.comment_action_failed)
                    is FeedListViewModel.LikeEvent.Updated -> null
                }
            }
        }
        LaunchedEffect(Unit) {
            feedViewModel.actionEvents.collect { event ->
                when (event) {
                    FeedListViewModel.ActionEvent.PostDeleted -> {
                        posts.refresh()
                        feedViewModel.reloadPinnedPost()
                        snackbarMessage = context.getString(R.string.post_deleted)
                    }
                    FeedListViewModel.ActionEvent.Error ->
                        snackbarMessage = context.getString(R.string.comment_action_failed)
                }
            }
        }

        FeedListScreen(
            posts = posts,
            pinnedPost = pinnedPost,
            loginRequired = loginRequired,
            interactionStates = interactionStates,
            revealedPosts = revealedPosts,
            nsfwAllowed = feedViewModel.nsfwAllowed,
            currentUserId = feedViewModel.currentUserId(),
            snackbarMessage = snackbarMessage,
            onSnackbarShown = { snackbarMessage = null },
            lazyListState = scrollState,
            onRefresh = { posts.refresh(); feedViewModel.reloadPinnedPost() },
            onPostClick = { post ->
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
                )
            },
            onLikeClick = { post, liked -> feedViewModel.togglePostLike(post, liked) },
            onRevealClick = { post -> feedViewModel.revealPost(post) },
            onMediaClick = { post ->
                val slug = post.mediaSlug ?: return@FeedListScreen
                val isAnime = post.mediaIsAnime ?: return@FeedListScreen
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    DetailsFragmentDirections.actionGlobalDetailsFragment(
                        type = if (isAnime) "anime" else "manga", slug = slug
                    )
                )
            },
            onEditClick = { post ->
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
                )
            },
            onDeleteClick = { post -> feedViewModel.deletePost(post) },
            onAuthorClick = { uid ->
                findNavController().navigateSafe(
                    R.id.user_profile_fragment,
                    UserProfileFragmentDirections.actionGlobalUserProfileFragment(uid)
                )
            }
        )
    }

    // region Navigation helpers

    private fun navigateToMediaDetails(media: Media) {
        findNavController().navigateSafe(
            R.id.user_profile_fragment,
            UserProfileFragmentDirections.actionGlobalDetailsFragment(media = media.toMediaDto())
        )
    }

    private fun navigateToCharacterDetails(character: Character) {
        findNavController().navigateSafe(
            R.id.user_profile_fragment,
            UserProfileFragmentDirections.actionGlobalCharacterDetailsBottomSheet(
                character.toCharacterDto()
            )
        )
    }

    private fun handleProfileLinkClick(link: ProfileLink) {
        val url = link.url ?: return
        if (URLUtil.isValidUrl(url)) {
            openUrl(url)
        } else {
            copyToClipboard(link.profileLinkSite?.name ?: "URL", url)
        }
    }

    // endregion
}
