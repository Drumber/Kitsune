package io.github.drumber.kitsune.ui.navigation.graph

import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.connectView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.createpost.ComposeSearchBoxView
import io.github.drumber.kitsune.ui.createpost.CreatePostScreen
import io.github.drumber.kitsune.ui.createpost.CreatePostViewModel
import io.github.drumber.kitsune.ui.createpost.MediaPickerScreen
import io.github.drumber.kitsune.ui.createpost.MediaPickerViewModel
import io.github.drumber.kitsune.ui.createpost.UnitPickerScreen
import io.github.drumber.kitsune.ui.createpost.UnitPickerViewModel
import io.github.drumber.kitsune.ui.feed.FeedListViewModel
import io.github.drumber.kitsune.ui.feed.FeedType
import io.github.drumber.kitsune.ui.feed.compose.FeedListScreen
import io.github.drumber.kitsune.ui.feed.compose.FeedScreen
import io.github.drumber.kitsune.ui.groupdetail.GroupDetailScreen
import io.github.drumber.kitsune.ui.groupdetail.GroupDetailViewModel
import io.github.drumber.kitsune.ui.groups.GroupsScreen
import io.github.drumber.kitsune.ui.groups.GroupsViewModel
import io.github.drumber.kitsune.ui.navigation.NavResultEffect
import io.github.drumber.kitsune.ui.navigation.NavResults
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.navigation.navigateSafe
import io.github.drumber.kitsune.ui.navigation.setNavResult
import io.github.drumber.kitsune.ui.notifications.NotificationsScreen
import io.github.drumber.kitsune.ui.notifications.NotificationsViewModel
import io.github.drumber.kitsune.ui.postdetail.PostDetailViewModel
import io.github.drumber.kitsune.ui.postdetail.compose.PostDetailScreen
import io.github.drumber.kitsune.ui.reactiondetail.ReactionDetailViewModel
import io.github.drumber.kitsune.ui.reactiondetail.compose.ReactionDetailScreen
import io.github.drumber.kitsune.ui.replies.RepliesViewModel
import io.github.drumber.kitsune.ui.replies.compose.RepliesScreen
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.socialGraph(navController: NavHostController) {
    composable<Routes.Feed> { backStackEntry ->
        FeedDestination(backStackEntry, navController)
    }
    composable<Routes.PostDetail> { backStackEntry ->
        PostDetailDestination(backStackEntry, navController)
    }
    composable<Routes.Replies> { backStackEntry ->
        RepliesDestination(backStackEntry, navController)
    }
    composable<Routes.ReactionDetail> { backStackEntry ->
        ReactionDetailDestination(backStackEntry, navController)
    }
    composable<Routes.Groups> {
        GroupsDestination(navController)
    }
    composable<Routes.GroupDetail> { backStackEntry ->
        GroupDetailDestination(backStackEntry, navController)
    }
    composable<Routes.CreatePost> { backStackEntry ->
        CreatePostDestination(backStackEntry, navController)
    }
    composable<Routes.Notifications> {
        NotificationsDestination(navController)
    }
}

// ---------------------------------------------------------------------------
// Feed
// ---------------------------------------------------------------------------

@Composable
private fun FeedDestination(backStackEntry: NavBackStackEntry, navController: NavHostController) {
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

    val postDeletedMsg = stringResource(R.string.post_deleted)
    val loginRequiredMsg = stringResource(R.string.comment_login_required)
    val actionFailedMsg = stringResource(R.string.comment_action_failed)

    LaunchedEffect(Unit) {
        globalVm.likeEvents.collect { event ->
            globalSnackbar = when (event) {
                FeedListViewModel.LikeEvent.LoginRequired -> loginRequiredMsg
                is FeedListViewModel.LikeEvent.Failed -> actionFailedMsg
                is FeedListViewModel.LikeEvent.Updated -> null
            }
        }
    }
    LaunchedEffect(Unit) {
        globalVm.actionEvents.collect { event ->
            globalSnackbar = when (event) {
                FeedListViewModel.ActionEvent.PostDeleted -> {
                    globalPosts.refresh()
                    postDeletedMsg
                }
                FeedListViewModel.ActionEvent.Error -> actionFailedMsg
            }
        }
    }
    LaunchedEffect(Unit) {
        followingVm.likeEvents.collect { event ->
            followingSnackbar = when (event) {
                FeedListViewModel.LikeEvent.LoginRequired -> loginRequiredMsg
                is FeedListViewModel.LikeEvent.Failed -> actionFailedMsg
                is FeedListViewModel.LikeEvent.Updated -> null
            }
        }
    }
    LaunchedEffect(Unit) {
        followingVm.actionEvents.collect { event ->
            followingSnackbar = when (event) {
                FeedListViewModel.ActionEvent.PostDeleted -> {
                    followingPosts.refresh()
                    postDeletedMsg
                }
                FeedListViewModel.ActionEvent.Error -> actionFailedMsg
            }
        }
    }

    backStackEntry.NavResultEffect<Boolean>(NavResults.POST_CREATED) {
        globalPosts.refresh()
        followingPosts.refresh()
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
        onNavigateToGroups = { navController.navigateSafe(Routes.Groups) },
        onNavigateToNotifications = { navController.navigateSafe(Routes.Notifications) },
        onCreatePost = { navController.navigateSafe(Routes.CreatePost()) },
        onPostClick = { post -> navController.navigateSafe(Routes.PostDetail(post.id)) },
        onLikeClick = { post, targetLiked, page ->
            if (page == 0) globalVm.togglePostLike(post, targetLiked)
            else followingVm.togglePostLike(post, targetLiked)
        },
        onRevealClick = { post, page ->
            if (page == 0) globalVm.revealPost(post) else followingVm.revealPost(post)
        },
        onMediaClick = { post ->
            val slug = post.mediaSlug
            val isAnime = post.mediaIsAnime
            if (!slug.isNullOrBlank() && isAnime != null) {
                navController.navigateSafe(Routes.Details(
                    type = if (isAnime) "anime" else "manga",
                    slug = slug
                ))
            }
        },
        onEditClick = { post -> navController.navigateSafe(Routes.CreatePost(editPostId = post.id)) },
        onDeleteClick = { post, page ->
            if (page == 0) globalVm.deletePost(post) else followingVm.deletePost(post)
        },
        onAuthorClick = { userId -> navController.navigateSafe(Routes.UserProfile(userId)) },
        onRefresh = { page ->
            if (page == 0) globalPosts.refresh() else followingPosts.refresh()
        }
    )
}

// ---------------------------------------------------------------------------
// Post detail
// ---------------------------------------------------------------------------

@Composable
private fun PostDetailDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController
) {
    val route = backStackEntry.toRoute<Routes.PostDetail>()
    val viewModel: PostDetailViewModel = koinViewModel()

    LaunchedEffect(route.postId) {
        viewModel.initFromPostId(route.postId)
    }

    val post by viewModel.postState.collectAsStateWithLifecycle()
    val postLikeState by viewModel.postLikeState.collectAsStateWithLifecycle()
    val revealedPosts by viewModel.revealedPosts.collectAsStateWithLifecycle(initialValue = emptySet())
    val composerMode by viewModel.composerMode.collectAsStateWithLifecycle()
    val comments = viewModel.comments.collectAsLazyPagingItems()
    var commentLikeOverrides by remember { mutableStateOf<Map<String, Pair<Boolean, Int>>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val loginRequiredMsg = stringResource(R.string.comment_login_required)
    val actionFailedMsg = stringResource(R.string.comment_action_failed)
    val commentPostedMsg = stringResource(R.string.comment_posted)
    val commentUpdatedMsg = stringResource(R.string.comment_updated)
    val commentDeletedMsg = stringResource(R.string.comment_deleted)
    val postDeletedMsg = stringResource(R.string.post_deleted)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PostDetailViewModel.Event.LoginRequired -> snackbarMessage = loginRequiredMsg
                PostDetailViewModel.Event.Error -> snackbarMessage = actionFailedMsg
                PostDetailViewModel.Event.CommentPosted -> {
                    viewModel.cancelComposer()
                    comments.refresh()
                    snackbarMessage = commentPostedMsg
                }
                PostDetailViewModel.Event.CommentUpdated -> {
                    viewModel.cancelComposer()
                    comments.refresh()
                    snackbarMessage = commentUpdatedMsg
                }
                PostDetailViewModel.Event.CommentDeleted -> {
                    comments.refresh()
                    snackbarMessage = commentDeletedMsg
                }
                PostDetailViewModel.Event.PostDeleted -> {
                    snackbarMessage = postDeletedMsg
                    navController.navigateUp()
                }
                is PostDetailViewModel.Event.CommentLikeChanged ->
                    commentLikeOverrides = commentLikeOverrides +
                        (event.commentId to Pair(event.isLiked, event.count))
            }
        }
    }

    backStackEntry.NavResultEffect<Boolean>(NavResults.POST_CREATED) {
        comments.refresh()
    }

    PostDetailScreen(
        post = post,
        postLikeState = postLikeState,
        isPostRevealed = (post?.id ?: route.postId) in revealedPosts,
        nsfwAllowed = viewModel.nsfwAllowed,
        comments = comments,
        commentLikeOverrides = commentLikeOverrides,
        composerMode = composerMode,
        currentUserId = viewModel.currentUserId(),
        snackbarMessage = snackbarMessage,
        onSnackbarShown = { snackbarMessage = null },
        onNavigateUp = { navController.navigateUp() },
        onPostLikeClick = { viewModel.togglePostLike() },
        onRevealPost = { viewModel.revealCurrentPost() },
        onMediaClick = { p ->
            val slug = p.mediaSlug
            val isAnime = p.mediaIsAnime
            if (!slug.isNullOrBlank() && isAnime != null) {
                navController.navigateSafe(Routes.Details(
                    type = if (isAnime) "anime" else "manga",
                    slug = slug
                ))
            }
        },
        onEditPost = { p -> navController.navigateSafe(Routes.CreatePost(editPostId = p.id)) },
        onDeletePost = { viewModel.deletePost() },
        onAuthorClick = { userId -> navController.navigateSafe(Routes.UserProfile(userId)) },
        onCommentLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
        onReplyClick = { comment -> viewModel.startReply(comment) },
        onViewAllRepliesClick = { comment ->
            navController.navigateSafe(Routes.Replies(
                postId = route.postId,
                parentCommentId = comment.id
            ))
        },
        onEditComment = { comment -> viewModel.startEditComment(comment) },
        onDeleteComment = { comment -> viewModel.deleteComment(comment.id) },
        onCancelComposer = { viewModel.cancelComposer() },
        onSubmitComment = { content ->
            when (val mode = viewModel.composerMode.value) {
                is PostDetailViewModel.ComposerMode.Edit ->
                    viewModel.updateComment(mode.comment.id, content)
                is PostDetailViewModel.ComposerMode.Reply ->
                    viewModel.postReply(mode.comment.id, content)
                PostDetailViewModel.ComposerMode.Normal ->
                    viewModel.postComment(content)
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Replies
// ---------------------------------------------------------------------------

@Composable
private fun RepliesDestination(backStackEntry: NavBackStackEntry, navController: NavHostController) {
    val route = backStackEntry.toRoute<Routes.Replies>()
    val viewModel: RepliesViewModel = koinViewModel(
        parameters = { parametersOf(route.parentCommentId, route.postId) }
    )

    val parentComment by viewModel.parentComment.collectAsStateWithLifecycle()
    val replies = viewModel.replies.collectAsLazyPagingItems()
    var parentIsLiked by remember { mutableStateOf(parentComment?.isLikedByMe ?: false) }
    var parentLikesCount by remember { mutableStateOf(parentComment?.likesCount ?: 0) }
    var commentLikeOverrides by remember { mutableStateOf<Map<String, Pair<Boolean, Int>>>(emptyMap()) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val commentPostedMsg = stringResource(R.string.comment_posted)
    val loginRequiredMsg = stringResource(R.string.comment_login_required)
    val actionFailedMsg = stringResource(R.string.comment_action_failed)

    LaunchedEffect(parentComment) {
        parentComment?.let {
            parentIsLiked = it.isLikedByMe
            parentLikesCount = it.likesCount
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RepliesViewModel.Event.CommentLikeChanged ->
                    if (event.commentId == route.parentCommentId) {
                        parentIsLiked = event.isLiked
                        parentLikesCount = event.count
                    } else {
                        commentLikeOverrides = commentLikeOverrides +
                            (event.commentId to Pair(event.isLiked, event.count))
                    }
                RepliesViewModel.Event.ReplyPosted -> {
                    replies.refresh()
                    snackbarMessage = commentPostedMsg
                }
                RepliesViewModel.Event.LoginRequired -> snackbarMessage = loginRequiredMsg
                RepliesViewModel.Event.Error -> snackbarMessage = actionFailedMsg
            }
        }
    }

    RepliesScreen(
        parentComment = parentComment,
        parentIsLiked = parentIsLiked,
        parentLikesCount = parentLikesCount,
        replies = replies,
        commentLikeOverrides = commentLikeOverrides,
        currentUserId = viewModel.currentUserId(),
        snackbarMessage = snackbarMessage,
        onSnackbarShown = { snackbarMessage = null },
        onNavigateUp = { navController.navigateUp() },
        onParentLikeClick = { parentComment?.let { viewModel.toggleCommentLike(it) } },
        onReplyLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
        onAuthorClick = { userId -> navController.navigateSafe(Routes.UserProfile(userId)) },
        onSubmitReply = { content -> viewModel.postReply(content) }
    )
}

// ---------------------------------------------------------------------------
// Reaction detail
// ---------------------------------------------------------------------------

@Composable
private fun ReactionDetailDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController
) {
    val route = backStackEntry.toRoute<Routes.ReactionDetail>()
    val viewModel: ReactionDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.reactionId) }
    )

    val reaction by viewModel.reaction.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isUpvoted by viewModel.isUpvoted.collectAsStateWithLifecycle()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val loginRequiredMsg = stringResource(R.string.reactions_upvote_login_required)
    val failedMsg = stringResource(R.string.reactions_upvote_failed)

    LaunchedEffect(Unit) {
        viewModel.upvoteEvents.collect { event ->
            snackbarMessage = when (event) {
                is ReactionDetailViewModel.UpvoteEvent.Success -> null
                ReactionDetailViewModel.UpvoteEvent.LoginRequired -> loginRequiredMsg
                ReactionDetailViewModel.UpvoteEvent.Failed -> failedMsg
            }
        }
    }

    ReactionDetailScreen(
        reaction = reaction,
        isLoading = isLoading,
        isUpvoted = isUpvoted,
        snackbarMessage = snackbarMessage,
        onSnackbarShown = { snackbarMessage = null },
        onNavigateUp = { navController.navigateUp() },
        onUpvote = { viewModel.upvote() },
        onMediaClick = {
            val r = viewModel.reaction.value
            val slug = r?.mediaSlug
            val isAnime = r?.mediaIsAnime
            if (!slug.isNullOrBlank() && isAnime != null) {
                navController.navigateSafe(Routes.Details(
                    type = if (isAnime) "anime" else "manga",
                    slug = slug
                ))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Groups
// ---------------------------------------------------------------------------

@Composable
private fun GroupsDestination(navController: NavHostController) {
    val viewModel: GroupsViewModel = koinViewModel()
    val groups = viewModel.dataSource.collectAsLazyPagingItems()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val isFollowingEnabled by viewModel.isFollowingEnabled.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    GroupsScreen(
        groups = groups,
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        isLoggedIn = viewModel.isLoggedIn,
        isFollowingEnabled = isFollowingEnabled,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onFollowingToggle = { viewModel.setFollowingEnabled(it) },
        onCategorySelect = { viewModel.setCategory(it) },
        onGroupClick = { group -> navController.navigateSafe(Routes.GroupDetail(group.id)) },
        onNavigateUp = { navController.navigateUp() },
        scrollToTopEvents = viewModel.scrollToTopRequested
    )
}

// ---------------------------------------------------------------------------
// Group detail
// ---------------------------------------------------------------------------

@Composable
private fun GroupDetailDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController
) {
    val route = backStackEntry.toRoute<Routes.GroupDetail>()
    val groupId = route.groupId
    val viewModel: GroupDetailViewModel = koinViewModel(
        parameters = { parametersOf(groupId) }
    )

    val context = LocalContext.current
    val group by viewModel.group.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val membershipState by viewModel.membershipState.collectAsStateWithLifecycle()

    // Signal passed into the embedded feed so it refreshes after a post is created/edited.
    val postCreatedRefresh = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    backStackEntry.NavResultEffect<Boolean>(NavResults.POST_CREATED) {
        postCreatedRefresh.tryEmit(Unit)
    }

    GroupDetailScreen(
        group = group,
        isLoading = isLoading,
        membershipState = membershipState,
        events = viewModel.events,
        isMemberDefault = membershipState.isMember,
        onNavigateUp = { navController.navigateUp() },
        onJoinLeave = { viewModel.toggleMembership() },
        onOpenCover = {
            group?.coverImageUrl?.let { url ->
                navController.navigateSafe(Routes.PhotoView(imageUrl = url, title = group?.name))
            }
        },
        onNavigateToCreatePost = {
            navController.navigateSafe(Routes.CreatePost(
                targetGroupId = groupId,
                targetGroupName = viewModel.group.value?.name
            ))
        },
        feedContent = {
            GroupEmbeddedFeedContent(
                groupId = groupId,
                navController = navController,
                postCreatedRefresh = postCreatedRefresh
            )
        }
    )
}

/** Renders the group feed inline, replacing the [FeedListFragment] that was embedded via AndroidFragment. */
@Composable
private fun GroupEmbeddedFeedContent(
    groupId: String,
    navController: NavHostController,
    postCreatedRefresh: MutableSharedFlow<Unit>
) {
    val feedVm: FeedListViewModel = koinViewModel(key = "group_feed_$groupId")

    LaunchedEffect(groupId) {
        feedVm.setGroupFeed(groupId)
    }

    val posts = feedVm.dataSource.collectAsLazyPagingItems()
    val pinnedPost by feedVm.pinnedPost.collectAsStateWithLifecycle()
    val loginRequired by feedVm.loginRequired.collectAsStateWithLifecycle(initialValue = false)
    val interactionStates by feedVm.interactionStates.collectAsStateWithLifecycle(initialValue = emptyMap())
    val revealedPosts by feedVm.revealedPosts.collectAsStateWithLifecycle(initialValue = emptySet())
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val postDeletedMsg = stringResource(R.string.post_deleted)
    val loginRequiredMsg = stringResource(R.string.comment_login_required)
    val actionFailedMsg = stringResource(R.string.comment_action_failed)

    LaunchedEffect(Unit) {
        feedVm.likeEvents.collect { event ->
            snackbarMessage = when (event) {
                FeedListViewModel.LikeEvent.LoginRequired -> loginRequiredMsg
                is FeedListViewModel.LikeEvent.Failed -> actionFailedMsg
                is FeedListViewModel.LikeEvent.Updated -> null
            }
        }
    }

    LaunchedEffect(Unit) {
        feedVm.actionEvents.collect { event ->
            snackbarMessage = when (event) {
                FeedListViewModel.ActionEvent.PostDeleted -> {
                    posts.refresh()
                    postDeletedMsg
                }
                FeedListViewModel.ActionEvent.Error -> actionFailedMsg
            }
        }
    }

    LaunchedEffect(Unit) {
        postCreatedRefresh.collect { posts.refresh() }
    }

    FeedListScreen(
        posts = posts,
        pinnedPost = pinnedPost,
        loginRequired = loginRequired,
        interactionStates = interactionStates,
        revealedPosts = revealedPosts,
        nsfwAllowed = feedVm.nsfwAllowed,
        currentUserId = feedVm.currentUserId(),
        snackbarMessage = snackbarMessage,
        onSnackbarShown = { snackbarMessage = null },
        onRefresh = { posts.refresh() },
        onPostClick = { post -> navController.navigateSafe(Routes.PostDetail(post.id)) },
        onLikeClick = { post, targetLiked -> feedVm.togglePostLike(post, targetLiked) },
        onRevealClick = { post -> feedVm.revealPost(post) },
        onMediaClick = { post ->
            val slug = post.mediaSlug
            val isAnime = post.mediaIsAnime
            if (!slug.isNullOrBlank() && isAnime != null) {
                navController.navigateSafe(Routes.Details(
                    type = if (isAnime) "anime" else "manga",
                    slug = slug
                ))
            }
        },
        onEditClick = { post -> navController.navigateSafe(Routes.CreatePost(editPostId = post.id)) },
        onDeleteClick = { post -> feedVm.deletePost(post) },
        onAuthorClick = { userId -> navController.navigateSafe(Routes.UserProfile(userId)) }
    )
}

// ---------------------------------------------------------------------------
// Create post
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController
) {
    val route = backStackEntry.toRoute<Routes.CreatePost>()
    val viewModel: CreatePostViewModel = koinViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(route) {
        route.editPostId?.let { viewModel.initFromPostId(it) }
        if (route.editPostId == null) {
            route.targetUserId?.let { viewModel.setWallTarget(it, route.targetUserName) }
            route.targetGroupId?.let { viewModel.setGroupTarget(it, route.targetGroupName) }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var imageEncodingFailed by remember { mutableStateOf(false) }
    var showMediaPicker by remember { mutableStateOf(false) }
    var showUnitPicker by remember { mutableStateOf(false) }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(CreatePostViewModel.MAX_IMAGES)
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            var failed = false
            for (uri in uris) {
                if (viewModel.uiState.value.images.size >= CreatePostViewModel.MAX_IMAGES) break
                val dataUri = encodeImageToBase64(context, uri)
                if (dataUri == null) { failed = true; continue }
                viewModel.addImage(uri.toString(), dataUri)
            }
            if (failed) imageEncodingFailed = true
        }
    }

    val legacyGetContents = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            var failed = false
            for (uri in uris) {
                if (viewModel.uiState.value.images.size >= CreatePostViewModel.MAX_IMAGES) break
                val dataUri = encodeImageToBase64(context, uri)
                if (dataUri == null) { failed = true; continue }
                viewModel.addImage(uri.toString(), dataUri)
            }
            if (failed) imageEncodingFailed = true
        }
    }

    CreatePostScreen(
        uiState = uiState,
        events = viewModel.events,
        imageEncodingError = imageEncodingFailed,
        onImageEncodingErrorShown = { imageEncodingFailed = false },
        onContentChange = viewModel::setContent,
        onSpoilerToggle = viewModel::setSpoiler,
        onNsfwToggle = viewModel::setNsfw,
        onTagMediaClick = { showMediaPicker = true },
        onTagUnitClick = { if (uiState.media != null) showUnitPicker = true },
        onClearMedia = viewModel::clearMedia,
        onClearUnit = viewModel::clearUnit,
        onAddImageClick = {
            if (!KitsunePref.forceLegacyImagePicker &&
                ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
            ) {
                pickImages.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                legacyGetContents.launch("image/*")
            }
        },
        onRemoveImage = viewModel::removeImage,
        onPublish = viewModel::publish,
        onNavigateUp = { navController.navigateUp() },
        onPublished = {
            navController.setNavResult(NavResults.POST_CREATED, true)
            navController.navigateUp()
        }
    )

    if (showMediaPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMediaPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            MediaPickerContent(
                onMediaSelected = { media ->
                    viewModel.setMedia(
                        CreatePostViewModel.SelectedMedia(
                            id = media.id,
                            title = media.title ?: "",
                            posterUrl = media.posterImageUrl,
                            isAnime = media is Anime
                        )
                    )
                    showMediaPicker = false
                }
            )
        }
    }

    val currentMedia = uiState.media
    if (showUnitPicker && currentMedia != null) {
        ModalBottomSheet(
            onDismissRequest = { showUnitPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            UnitPickerContent(
                mediaId = currentMedia.id,
                isAnime = currentMedia.isAnime,
                posterUrl = currentMedia.posterUrl,
                onUnitSelected = { unit ->
                    unit.id?.let { id ->
                        viewModel.setUnit(
                            CreatePostViewModel.SelectedUnit(
                                id = id,
                                number = unit.number ?: 0,
                                title = unit.title(context) ?: "",
                                isEpisode = unit is Episode
                            )
                        )
                    }
                    showUnitPicker = false
                }
            )
        }
    }
}

/** Media picker bottom-sheet content backed by [MediaPickerViewModel] and Algolia InstantSearch. */
@Composable
private fun MediaPickerContent(onMediaSelected: (Media) -> Unit) {
    val viewModel: MediaPickerViewModel = koinViewModel()
    val connectionHandler = remember { ConnectionHandler() }
    val composeSearchBoxView = remember { ComposeSearchBoxView() }

    val searchBox by viewModel.searchBox.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val searchItems = viewModel.searchResultSource.collectAsLazyPagingItems()
    val query by composeSearchBoxView.query

    LaunchedEffect(searchBox) {
        connectionHandler.clear()
        searchBox?.let { sb -> connectionHandler += sb.connectView(composeSearchBoxView) }
    }

    DisposableEffect(Unit) {
        onDispose { connectionHandler.clear() }
    }

    MediaPickerScreen(
        searchQuery = query,
        onSearchQueryChange = composeSearchBoxView::change,
        searchItems = searchItems,
        status = status,
        onMediaClick = onMediaSelected
    )
}

/** Unit picker bottom-sheet content backed by [UnitPickerViewModel]. */
@Composable
private fun UnitPickerContent(
    mediaId: String,
    isAnime: Boolean,
    posterUrl: String?,
    onUnitSelected: (MediaUnit) -> Unit
) {
    val viewModel: UnitPickerViewModel = koinViewModel()
    val units = viewModel.unitPager(mediaId, isAnime).collectAsLazyPagingItems()

    UnitPickerScreen(
        units = units,
        posterUrl = posterUrl,
        onUnitClick = onUnitSelected
    )
}

// ---------------------------------------------------------------------------
// Notifications
// ---------------------------------------------------------------------------

@Composable
private fun NotificationsDestination(navController: NavHostController) {
    val viewModel: NotificationsViewModel = koinViewModel()
    val notifications = viewModel.notifications.collectAsLazyPagingItems()

    NotificationsScreen(
        notifications = notifications,
        loginRequired = viewModel.loginRequired,
        onNavigateUp = { navController.navigateUp() },
        onNotificationClick = { notification ->
            val reactionId = notification.targetReactionId
            if (reactionId != null) {
                navController.navigateSafe(Routes.ReactionDetail(reactionId))
            } else {
                notification.targetPost?.let { post ->
                    navController.navigateSafe(Routes.PostDetail(post.id))
                }
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private suspend fun encodeImageToBase64(context: android.content.Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext null
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        try {
            inputStream.use { stream ->
                val bytes = stream.readBytes()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            }.let { base64 -> "data:$mimeType;base64,$base64" }
        } catch (e: Exception) {
            logE("Error while encoding image to Base64 from uri: $uri", e)
            null
        }
    }
