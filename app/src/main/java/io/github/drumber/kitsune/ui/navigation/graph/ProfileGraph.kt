package io.github.drumber.kitsune.ui.navigation.graph

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.chibatching.kotpref.livedata.asLiveData
import com.google.android.material.color.DynamicColors
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.AppUpdateRepository
import io.github.drumber.kitsune.data.presentation.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.domain.work.UpdateLibraryWidgetUseCase
import io.github.drumber.kitsune.notification.Notifications
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.details.characters.CharacterDetailsSheet
import io.github.drumber.kitsune.ui.feed.FeedListViewModel
import io.github.drumber.kitsune.ui.feed.compose.FeedListScreen
import io.github.drumber.kitsune.ui.navigation.LocalReselectEvents
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.navigation.followListType
import io.github.drumber.kitsune.ui.navigation.navigateSafe
import io.github.drumber.kitsune.ui.navigation.routeValue
import io.github.drumber.kitsune.ui.permissions.isNotificationPermissionGranted
import io.github.drumber.kitsune.ui.permissions.requestNotificationPermission
import io.github.drumber.kitsune.ui.photoview.PhotoViewActivity
import io.github.drumber.kitsune.ui.photoview.openPhotoView
import io.github.drumber.kitsune.ui.profile.MyProfileViewModel
import io.github.drumber.kitsune.ui.profile.ProfileScreen
import io.github.drumber.kitsune.ui.profile.ProfileUiState
import io.github.drumber.kitsune.ui.profile.UserProfileUiState
import io.github.drumber.kitsune.ui.profile.UserProfileViewModel
import io.github.drumber.kitsune.ui.profile.about.ProfileAboutScreen
import io.github.drumber.kitsune.ui.profile.editprofile.EditProfileLinkScreen
import io.github.drumber.kitsune.ui.profile.editprofile.EditProfileScreen
import io.github.drumber.kitsune.ui.profile.editprofile.EditProfileViewModel
import io.github.drumber.kitsune.ui.profile.editprofile.ImagePickerType
import io.github.drumber.kitsune.ui.profile.editprofile.ProfileImageContainer
import io.github.drumber.kitsune.ui.profile.editprofile.ProfileLinkAction
import io.github.drumber.kitsune.ui.profile.editprofile.ProfileLinkEntry
import io.github.drumber.kitsune.ui.profile.editprofile.SelectProfileLinkSiteScreen
import io.github.drumber.kitsune.ui.profile.follow.FollowListScreen
import io.github.drumber.kitsune.ui.profile.follow.FollowListViewModel
import io.github.drumber.kitsune.ui.settings.AppLogsScreen
import io.github.drumber.kitsune.ui.settings.AppLogsViewModel
import io.github.drumber.kitsune.ui.settings.AppearanceScreen
import io.github.drumber.kitsune.ui.settings.AppearanceUiState
import io.github.drumber.kitsune.ui.settings.OSLibrariesScreen
import io.github.drumber.kitsune.ui.settings.SettingsCallbacks
import io.github.drumber.kitsune.ui.settings.SettingsScreen
import io.github.drumber.kitsune.ui.settings.SettingsUiState
import io.github.drumber.kitsune.ui.settings.SettingsViewModel
import io.github.drumber.kitsune.ui.webview.WebViewScreen
import androidx.core.content.FileProvider
import io.github.drumber.kitsune.util.LogCatReader
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.toDate
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Registers the profile / settings / WebView area destinations into the Compose [NavHost].
 *
 * **scrollToTopEvents**: Each profile destination supplies a fresh [MutableSharedFlow]. Nav-bar
 * reselect → scroll-to-top is wired to the flow; the caller that owns the nav bar re-select event
 * should emit into it once per session (plumbing left for a future integration step).
 *
 * **WebView navigation-bar hiding**: The original [WebViewFragment] hid the navigation bar via
 * `WindowInsetsController`. This destination does not replicate that effect; the shell layer
 * should apply a `SystemUiController` side-effect at this destination if required.
 */
@Suppress("LongMethod")
fun NavGraphBuilder.profileGraph(navController: NavHostController) {

    composable<Routes.MyProfile> {
        MyProfileDestination(navController)
    }

    composable<Routes.UserProfile> { entry ->
        val route = entry.toRoute<Routes.UserProfile>()
        UserProfileDestination(navController, route.userId, route.userName)
    }

    composable<Routes.FollowList> { entry ->
        val route = entry.toRoute<Routes.FollowList>()
        FollowListDestination(navController, route.userId, route.followListType(), route.userName)
    }

    composable<Routes.EditProfile> {
        EditProfileDestination(navController)
    }

    composable<Routes.WebView> { entry ->
        val route = entry.toRoute<Routes.WebView>()
        WebViewDestination(navController, route.url)
    }

    navigation<Routes.SettingsGraph>(startDestination = Routes.Settings) {
        composable<Routes.Settings> {
            SettingsDestination(navController)
        }
        composable<Routes.Appearance> {
            AppearanceDestination(navController)
        }
        composable<Routes.OSLibraries> {
            OSLibrariesScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable<Routes.AppLogs> {
            AppLogsDestination(navController)
        }
    }
}

// ---------------------------------------------------------------------------
// MyProfile
// ---------------------------------------------------------------------------

@Composable
private fun MyProfileDestination(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MyProfileViewModel = koinViewModel()
    val user by viewModel.userModel.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // scrollToTopEvents: a fresh flow per composition; nav-bar reselect plumbing is deferred.
    val scrollToTopEvents = LocalReselectEvents.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedCharacterId by remember { mutableStateOf<String?>(null) }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                scope.launch { viewModel.logOut() }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (selectedCharacterId != null) {
        CharacterDetailsSheet(
            characterId = selectedCharacterId!!,
            onDismiss = { selectedCharacterId = null },
            onNavigateToMedia = { mediaId, isAnime ->
                selectedCharacterId = null
                navController.navigateSafe(Routes.Details(mediaId = mediaId, isAnime = isAnime))
            },
            onFavoriteChanged = { viewModel.refreshUser() }
        )
    }

    val displayName = user?.name ?: stringResource(R.string.not_logged_in)

    ProfileScreen(
        user = user,
        displayName = displayName,
        subtitle = null,
        isMyProfile = true,
        uiState = uiState,
        scrollToTopEvents = scrollToTopEvents,
        aboutTabContent = { scrollState ->
            MyProfileAboutTab(
                user = user,
                uiState = uiState,
                scrollState = scrollState,
                viewModel = viewModel,
                navController = navController,
                onCharacterClick = { character -> selectedCharacterId = character.id }
            )
        },
        feedTabContent = { scrollState ->
            ProfileFeedTab(
                userId = user?.id,
                feedKey = "my_profile_feed",
                scrollState = scrollState,
                navController = navController
            )
        },
        onShareProfile = {
            val profileId = viewModel.getUser()?.slug ?: viewModel.getUser()?.id
            if (profileId != null) {
                context.shareUrl(Kitsu.USER_URL_PREFIX + profileId)
            } else {
                Toast.makeText(context, R.string.error_something_wrong, Toast.LENGTH_SHORT).show()
            }
        },
        onPostOnWall = {
            navController.navigateSafe(Routes.CreatePost())
        },
        onCoverClick = {
            val url = viewModel.getUser()?.coverImage?.originalOrDown() ?: return@ProfileScreen
            context.openPhotoView(url, viewModel.getUser()?.name?.let { "$it Cover" })
        },
        onAvatarClick = {
            val url = viewModel.getUser()?.avatar?.originalOrDown() ?: return@ProfileScreen
            context.openPhotoView(url, viewModel.getUser()?.name?.let { "$it Avatar" })
        },
        onNavigateToSettings = {
            navController.navigateSafe(Routes.SettingsGraph)
        },
        onNavigateToEditProfile = {
            navController.navigateSafe(Routes.EditProfile)
        },
        onLogOut = { showLogoutDialog = true },
        onSignIn = {
            context.startActivity(Intent(context, AuthenticationActivity::class.java))
        },
        onNavigateUp = { navController.navigateUp() }
    )
}

@Composable
private fun MyProfileAboutTab(
    user: User?,
    uiState: ProfileUiState,
    scrollState: LazyListState,
    viewModel: MyProfileViewModel,
    navController: NavHostController,
    onCharacterClick: (Character) -> Unit
) {
    val context = LocalContext.current
    ProfileAboutScreen(
        user = user,
        isRefreshing = uiState.isRefreshing,
        isInitialLoading = uiState.isInitialLoading,
        followState = null,
        lazyListState = scrollState,
        onRefresh = viewModel::refreshUser,
        onFollowingClick = {
            val u = viewModel.getUser() ?: return@ProfileAboutScreen
            navController.navigateSafe(
                Routes.FollowList(u.id, FollowListType.FOLLOWING.routeValue(), u.name)
            )
        },
        onFollowersClick = {
            val u = viewModel.getUser() ?: return@ProfileAboutScreen
            navController.navigateSafe(
                Routes.FollowList(u.id, FollowListType.FOLLOWERS.routeValue(), u.name)
            )
        },
        onWaifuClick = onCharacterClick,
        onMediaClick = { media -> navController.navigateToMediaDetails(media) },
        onCharacterClick = onCharacterClick,
        onProfileLinkClick = { link -> context.handleProfileLinkClick(link) }
    )
}

// ---------------------------------------------------------------------------
// UserProfile
// ---------------------------------------------------------------------------

@Composable
private fun UserProfileDestination(
    navController: NavHostController,
    userId: String,
    userName: String?
) {
    val context = LocalContext.current
    val viewModel: UserProfileViewModel = koinViewModel(
        parameters = { parametersOf(userId) }
    )
    val user by viewModel.userModel.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scrollToTopEvents = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    var selectedCharacterId by remember { mutableStateOf<String?>(null) }

    if (selectedCharacterId != null) {
        CharacterDetailsSheet(
            characterId = selectedCharacterId!!,
            onDismiss = { selectedCharacterId = null },
            onNavigateToMedia = { mediaId, isAnime ->
                selectedCharacterId = null
                navController.navigateSafe(Routes.Details(mediaId = mediaId, isAnime = isAnime))
            },
            onFavoriteChanged = { viewModel.refreshUser() }
        )
    }

    val displayName = user?.name
        ?: user?.slug
        ?: userName
        ?: stringResource(R.string.nav_profile)
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
        aboutTabContent = { scrollState ->
            UserProfileAboutTab(
                user = user,
                uiState = uiState,
                scrollState = scrollState,
                viewModel = viewModel,
                navController = navController,
                onCharacterClick = { character -> selectedCharacterId = character.id }
            )
        },
        feedTabContent = { scrollState ->
            ProfileFeedTab(
                userId = user?.id ?: userId,
                feedKey = "user_profile_feed",
                scrollState = scrollState,
                navController = navController
            )
        },
        onShareProfile = {
            val profileId = viewModel.getUser()?.slug ?: viewModel.getUser()?.id ?: userId
            context.shareUrl(Kitsu.USER_URL_PREFIX + profileId)
        },
        onPostOnWall = {
            val u = viewModel.getUser() ?: return@ProfileScreen
            navController.navigateSafe(
                Routes.CreatePost(targetUserId = u.id, targetUserName = u.name)
            )
        },
        onCoverClick = {
            val url = viewModel.getUser()?.coverImage?.originalOrDown() ?: return@ProfileScreen
            context.openPhotoView(url, viewModel.getUser()?.name?.let { "$it Cover" })
        },
        onAvatarClick = {
            val url = viewModel.getUser()?.avatar?.originalOrDown() ?: return@ProfileScreen
            context.openPhotoView(url, viewModel.getUser()?.name?.let { "$it Avatar" })
        },
        onNavigateToSettings = {},
        onNavigateToEditProfile = {},
        onLogOut = {},
        onSignIn = {},
        onNavigateUp = { navController.navigateUp() }
    )
}

@Composable
private fun UserProfileAboutTab(
    user: User?,
    uiState: ProfileUiState,
    scrollState: LazyListState,
    viewModel: UserProfileViewModel,
    navController: NavHostController,
    onCharacterClick: (Character) -> Unit
) {
    val userUiState = uiState as? UserProfileUiState
    val context = LocalContext.current
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
            navController.navigateSafe(
                Routes.FollowList(u.id, FollowListType.FOLLOWING.routeValue(), u.name)
            )
        },
        onFollowersClick = {
            val u = viewModel.getUser() ?: return@ProfileAboutScreen
            navController.navigateSafe(
                Routes.FollowList(u.id, FollowListType.FOLLOWERS.routeValue(), u.name)
            )
        },
        onWaifuClick = onCharacterClick,
        onMediaClick = { media -> navController.navigateToMediaDetails(media) },
        onCharacterClick = onCharacterClick,
        onProfileLinkClick = { link -> context.handleProfileLinkClick(link) }
    )
}

// ---------------------------------------------------------------------------
// Shared: feed tab
// ---------------------------------------------------------------------------

@Composable
private fun ProfileFeedTab(
    userId: String?,
    feedKey: String,
    scrollState: LazyListState,
    navController: NavHostController
) {
    if (userId == null) return

    val feedViewModel: FeedListViewModel = koinViewModel(key = feedKey)
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
            navController.navigateSafe(Routes.PostDetail(postId = post.id))
        },
        onLikeClick = { post, liked -> feedViewModel.togglePostLike(post, liked) },
        onRevealClick = { post -> feedViewModel.revealPost(post) },
        onMediaClick = { post ->
            val slug = post.mediaSlug ?: return@FeedListScreen
            val isAnime = post.mediaIsAnime ?: return@FeedListScreen
            navController.navigateSafe(
                Routes.Details(
                    type = if (isAnime) "anime" else "manga",
                    slug = slug
                )
            )
        },
        onEditClick = { post ->
            navController.navigateSafe(Routes.CreatePost(editPostId = post.id))
        },
        onDeleteClick = { post -> feedViewModel.deletePost(post) },
        onAuthorClick = { uid ->
            navController.navigateSafe(Routes.UserProfile(userId = uid))
        }
    )
}

// ---------------------------------------------------------------------------
// FollowList
// ---------------------------------------------------------------------------

@Composable
private fun FollowListDestination(
    navController: NavHostController,
    userId: String,
    listType: FollowListType,
    userName: String?
) {
    val viewModel: FollowListViewModel = koinViewModel(
        parameters = { parametersOf(userId, listType) }
    )
    val users = viewModel.users.collectAsLazyPagingItems()
    val followStates by viewModel.followStates.collectAsStateWithLifecycle()

    val title = when (listType) {
        FollowListType.FOLLOWING -> stringResource(R.string.follow_list_following_title)
        FollowListType.FOLLOWERS -> stringResource(R.string.follow_list_followers_title)
    }

    FollowListScreen(
        title = title,
        users = users,
        followStates = followStates,
        onNavigateUp = { navController.navigateUp() },
        onUserClick = { uid ->
            navController.navigateSafe(Routes.UserProfile(userId = uid))
        },
        onFollowClick = { uid -> viewModel.toggleFollow(uid) },
        onResolveFollowState = { uid -> viewModel.resolveFollowState(uid) },
        showButtonFor = { uid -> viewModel.showButtonFor(uid) }
    )
}

// ---------------------------------------------------------------------------
// EditProfile
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDestination(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: EditProfileViewModel = koinViewModel()

    // Navigate away immediately if no user is logged in.
    LaunchedEffect(Unit) {
        if (!viewModel.hasUser()) {
            Toast.makeText(context, R.string.error_invalid_user, Toast.LENGTH_LONG).show()
            navController.navigateUp()
        }
    }

    // Image picker launchers
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val state = viewModel.profileImageState
            viewModel.acceptProfileImageChanges(
                when (viewModel.currentImagePickerType) {
                    ImagePickerType.AVATAR -> state.copy(selectedAvatarUri = uri)
                    ImagePickerType.COVER -> state.copy(selectedCoverUri = uri)
                    else -> state
                }
            )
        }
        viewModel.currentImagePickerType = null
    }
    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val state = viewModel.profileImageState
            viewModel.acceptProfileImageChanges(
                when (viewModel.currentImagePickerType) {
                    ImagePickerType.AVATAR -> state.copy(selectedAvatarUri = uri)
                    ImagePickerType.COVER -> state.copy(selectedCoverUri = uri)
                    else -> state
                }
            )
        }
        viewModel.currentImagePickerType = null
    }

    fun openImagePicker(type: ImagePickerType) {
        viewModel.currentImagePickerType = type
        if (!KitsunePref.forceLegacyImagePicker
            && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
        ) {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            getContentLauncher.launch("image/*")
        }
    }

    // Date picker state
    var birthdayPickerTargetMs by remember { mutableStateOf<Long?>(null) }
    if (birthdayPickerTargetMs != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthdayPickerTargetMs
        )
        DatePickerDialog(
            onDismissRequest = { birthdayPickerTargetMs = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val dateString = ms.toDate().formatDate("yyyy-MM-dd")
                        viewModel.acceptProfileChanges(
                            viewModel.profileState.copy(birthday = dateString)
                        )
                    }
                    birthdayPickerTargetMs = null
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { birthdayPickerTargetMs = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Profile link bottom-sheet state
    // selectSiteSheet: visible when user taps "Add profile link"
    var selectSiteSheetVisible by remember { mutableStateOf(false) }
    // editLinkSheet: (entry, isCreatingNew) pair while the edit/add sheet is open
    var editLinkSheet by remember { mutableStateOf<Pair<ProfileLinkEntry, Boolean>?>(null) }

    if (selectSiteSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val profileLinkSites by viewModel.profileLinkSitesFlow.collectAsStateWithLifecycle(
            initialValue = emptyList()
        )
        val isLoading by viewModel.profileLinkSitesLoadStateFlow.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { viewModel.loadProfileLinkSites() }

        val availableSites = profileLinkSites.filter { site ->
            viewModel.profileLinkEntries.none { it.site.id == site.id }
        }

        ModalBottomSheet(
            onDismissRequest = { selectSiteSheetVisible = false },
            sheetState = sheetState
        ) {
            SelectProfileLinkSiteScreen(
                profileLinkSites = availableSites,
                isLoading = isLoading,
                onSiteSelected = { site: ProfileLinkSite ->
                    selectSiteSheetVisible = false
                    editLinkSheet = ProfileLinkEntry(null, "", site) to true
                }
            )
        }
    }

    editLinkSheet?.let { (entry, isCreatingNew) ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { editLinkSheet = null },
            sheetState = sheetState
        ) {
            EditProfileLinkScreen(
                profileLinkEntry = entry,
                isCreatingNew = isCreatingNew,
                onConfirm = { url ->
                    viewModel.acceptProfileLinkAction(
                        ProfileLinkAction.Edit(entry.copy(url = url))
                    )
                    editLinkSheet = null
                },
                onDelete = {
                    viewModel.acceptProfileLinkAction(ProfileLinkAction.Delete(entry))
                    editLinkSheet = null
                },
                onCancel = { editLinkSheet = null }
            )
        }
    }

    EditProfileScreen(
        viewModel = viewModel,
        onDismiss = { navController.navigateUp() },
        onAvatarClick = { openImagePicker(ImagePickerType.AVATAR) },
        onCoverClick = { openImagePicker(ImagePickerType.COVER) },
        onBirthdayClick = { currentDateMs ->
            birthdayPickerTargetMs = currentDateMs
        },
        onAddProfileLink = { selectSiteSheetVisible = true },
        onEditProfileLink = { entry -> editLinkSheet = entry to false },
        onSaveClick = {
            val imageUpload = createUserImageUpload(context, viewModel)
            viewModel.updateUserProfile(imageUpload)
        }
    )
}

// ---------------------------------------------------------------------------
// WebView
// ---------------------------------------------------------------------------

@Composable
private fun WebViewDestination(navController: NavHostController, url: String) {
    val context = LocalContext.current
    val accessTokenRepository: AccessTokenRepository =
        koinInject()

    // The WebView reference is retained so BackHandler can call goBack().
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    WebViewScreen(
        initialUrl = url,
        // savedInstanceState is not available in Compose nav; the WebView reloads the URL.
        savedInstanceState = null,
        getAccessToken = accessTokenRepository::getAccessToken,
        onNavigateUp = { navController.navigateUp() },
        onWebViewReady = { wv -> webView = wv },
        openUrl = { u ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))
            } catch (e: Exception) {
                context.logE("Failed to open URL: $u", e)
            }
        },
        copyToClipboard = { label, text -> context.copyToClipboard(label, text) }
    )
}

// ---------------------------------------------------------------------------
// Settings
// ---------------------------------------------------------------------------

@Suppress("LongMethod")
@Composable
private fun SettingsDestination(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: SettingsViewModel = koinViewModel()
    val appUpdateRepository: AppUpdateRepository = koinInject()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(viewModel) {
        viewModel.errorMessageListener = { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error: ${error.getMessage(context)}",
                    actionLabel = context.getString(R.string.action_dismiss),
                    duration = SnackbarDuration.Long
                )
            }
        }
        onDispose { viewModel.errorMessageListener = null }
    }

    val requestNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            KitsunePref.flagUserDeniedNotificationPermission = false
        } else {
            KitsunePref.flagUserDeniedNotificationPermission = true
            Toast.makeText(
                context,
                R.string.error_requires_notification_permission,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val userState by viewModel.userModel.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle(false)
    val startFragment by KitsunePref.asLiveData(KitsunePref::startFragment)
        .collectAsStateWithLifecycle(KitsunePref.startFragment)
    val rememberSearchFilters by KitsunePref.asLiveData(KitsunePref::rememberSearchFilters)
        .collectAsStateWithLifecycle(KitsunePref.rememberSearchFilters)
    val doubleBackToExit by KitsunePref.asLiveData(KitsunePref::doubleBackToExit)
        .collectAsStateWithLifecycle(KitsunePref.doubleBackToExit)
    val forceLegacyImagePicker by KitsunePref.asLiveData(KitsunePref::forceLegacyImagePicker)
        .collectAsStateWithLifecycle(KitsunePref.forceLegacyImagePicker)
    val checkForUpdatesOnStart by KitsunePref.asLiveData(KitsunePref::checkForUpdatesOnStart)
        .collectAsStateWithLifecycle(KitsunePref.checkForUpdatesOnStart)
    val titles by KitsunePref.getTitleLanguageAsFlow()
        .collectAsStateWithLifecycle(KitsunePref.titles)

    val uiState = SettingsUiState(
        user = userState,
        isLoading = isLoading ?: false,
        titles = titles,
        startFragment = startFragment,
        rememberSearchFilters = rememberSearchFilters,
        doubleBackToExit = doubleBackToExit,
        isPhotoPickerAvailable =
            ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context),
        forceLegacyImagePicker = forceLegacyImagePicker,
        checkForUpdatesOnStart = checkForUpdatesOnStart,
        appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    )

    val callbacks = SettingsCallbacks(
        onNavigateUp = { navController.navigateUp() },
        onNavigateToAppearance = {
            navController.navigateSafe(Routes.Appearance)
        },
        onNavigateToAppLogs = {
            navController.navigateSafe(Routes.AppLogs)
        },
        onNavigateToLibraries = {
            navController.navigateSafe(Routes.OSLibraries)
        },
        onNavigateToGitHub = {
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_repo_url)))
                )
            } catch (e: Exception) {
                context.logE("Failed to open GitHub URL.", e)
            }
        },
        onLanguageSelected = { tag ->
            val localeList = if (tag.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(tag)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        },
        onStartFragmentSelected = { pref -> KitsunePref.startFragment = pref },
        onTitlesSelected = { pref ->
            val old = KitsunePref.titles
            KitsunePref.titles = pref
            if (userState != null && old != pref) {
                viewModel.updateUser(
                    LocalUser.empty(userState!!.id).copy(titleLanguagePreference = pref)
                )
            }
        },
        onCountrySelected = { code ->
            userState?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(country = code)) }
        },
        onSfwFilterSelected = { pref ->
            userState?.let {
                viewModel.updateUser(LocalUser.empty(it.id).copy(sfwFilterPreference = pref))
            }
        },
        onRatingSystemSelected = { pref ->
            userState?.let {
                viewModel.updateUser(LocalUser.empty(it.id).copy(ratingSystem = pref))
            }
        },
        onDisplayNameChanged = { name ->
            userState?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(name = name)) }
        },
        onProfileUrlChanged = { slug ->
            userState?.let { viewModel.updateUser(LocalUser.empty(it.id).copy(slug = slug)) }
        },
        onRememberSearchFiltersToggle = { KitsunePref.rememberSearchFilters = it },
        onDoubleBackToExitToggle = { KitsunePref.doubleBackToExit = it },
        onForceLegacyImagePickerToggle = { KitsunePref.forceLegacyImagePicker = it },
        onCheckForUpdatesToggle = { enabled ->
            if (enabled && !context.isNotificationPermissionGranted()) {
                val activity = context as? Activity
                activity?.requestNotificationPermission(requestNotificationPermissionLauncher)
                    ?: requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                KitsunePref.checkForUpdatesOnStart = enabled
            }
        },
        onAppVersionClick = {
            Toast.makeText(
                context,
                R.string.info_update_checking_new_version,
                Toast.LENGTH_SHORT
            ).show()
            scope.launch {
                when (val result = appUpdateRepository.checkForUpdates(BuildConfig.VERSION_NAME)) {
                    is UpdateCheckResult.NewVersion -> {
                        val release = result.release
                        Notifications.showNewVersion(context, release)
                        val message = context.getString(
                            R.string.info_update_new_version_available_text,
                            release.version
                        )
                        val snackResult = snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = context.getString(R.string.action_view),
                            duration = SnackbarDuration.Long
                        )
                        if (snackResult == SnackbarResult.ActionPerformed) {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(release.url))
                                )
                            } catch (e: Exception) {
                                logE("Failed to open release URL.", e)
                            }
                        }
                    }
                    is UpdateCheckResult.NoNewVersion -> {
                        Toast.makeText(
                            context,
                            R.string.info_update_no_new_version_available,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is UpdateCheckResult.Error -> {
                        Toast.makeText(
                            context,
                            R.string.info_update_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    )

    SettingsWithSnackbar(
        uiState = uiState,
        callbacks = callbacks,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun SettingsWithSnackbar(
    uiState: SettingsUiState,
    callbacks: SettingsCallbacks,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(uiState = uiState, callbacks = callbacks)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ---------------------------------------------------------------------------
// Appearance
// ---------------------------------------------------------------------------

@Composable
private fun AppearanceDestination(navController: NavHostController) {
    val context = LocalContext.current
    val updateLibraryWidget: UpdateLibraryWidgetUseCase = koinInject()

    val useDynamicColorTheme by KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme)
        .collectAsStateWithLifecycle(KitsunePref.useDynamicColorTheme)
    val appTheme by KitsunePref.asLiveData(KitsunePref::appTheme)
        .collectAsStateWithLifecycle(KitsunePref.appTheme)
    val darkMode by KitsunePref.asLiveData(KitsunePref::darkMode)
        .collectAsStateWithLifecycle(KitsunePref.darkMode)
    val oledBlackMode by KitsunePref.asLiveData(KitsunePref::oledBlackMode)
        .collectAsStateWithLifecycle(KitsunePref.oledBlackMode)
    val mediaItemSize by KitsunePref.asLiveData(KitsunePref::mediaItemSize)
        .collectAsStateWithLifecycle(KitsunePref.mediaItemSize)

    AppearanceScreen(
        uiState = AppearanceUiState(
            isDynamicColorAvailable = DynamicColors.isDynamicColorAvailable(),
            useDynamicColorTheme = useDynamicColorTheme,
            appTheme = appTheme,
            darkMode = darkMode,
            oledBlackMode = oledBlackMode,
            mediaItemSize = mediaItemSize
        ),
        onNavigateUp = { navController.navigateUp() },
        onDynamicColorToggle = { enabled ->
            KitsunePref.useDynamicColorTheme = enabled
            updateLibraryWidget(context)
        },
        onThemeSelected = { theme ->
            KitsunePref.appTheme = theme
            updateLibraryWidget(context)
        },
        onDarkModeSelected = { value ->
            if (KitsunePref.darkMode != value) {
                KitsunePref.darkMode = value
                AppCompatDelegate.setDefaultNightMode(value.toInt())
            }
        },
        onOledBlackToggle = { enabled -> KitsunePref.oledBlackMode = enabled },
        onMediaItemSizeSelected = { size -> KitsunePref.mediaItemSize = size }
    )
}

// ---------------------------------------------------------------------------
// AppLogs
// ---------------------------------------------------------------------------

@Composable
private fun AppLogsDestination(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: AppLogsViewModel = koinViewModel()
    val logs by viewModel.logMessages.collectAsStateWithLifecycle()

    AppLogsScreen(
        logs = logs,
        onNavigateUp = { navController.navigateUp() },
        onShareClick = {
            scope.launch {
                shareLogFile(context)
            }
        }
    )
}

@SuppressLint("SimpleDateFormat")
private suspend fun shareLogFile(context: android.content.Context) {
    val dateTime = SimpleDateFormat("yyy-MM-dd_HH-mm-ss").format(Date())
    val fileName = "Kitsune_$dateTime.txt"
    val logsDir = File(context.cacheDir, "logs")
    val logFile = File(logsDir, fileName)

    logsDir.listFiles { file: File -> file.isFile }?.forEach { it.delete() }
    logFile.deleteOnExit()

    LogCatReader.writeAppLogsToFile(logFile)

    val contentUri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        logFile
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/*"
        putExtra(Intent.EXTRA_STREAM, contentUri)
    }
    context.startActivity(
        Intent.createChooser(shareIntent, context.getString(R.string.action_share_app_logs))
    )
}

// ---------------------------------------------------------------------------
// Shared UI: logout dialog
// ---------------------------------------------------------------------------

@Composable
private fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_log_out)) },
        text = { Text(stringResource(R.string.dialog_log_out_confirmation)) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_log_out))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

private fun NavHostController.navigateToMediaDetails(media: Media) {
    navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
}


private fun android.content.Context.shareUrl(url: String) {
    startActivity(
        Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            },
            null
        )
    )
}

private fun android.content.Context.handleProfileLinkClick(link: ProfileLink) {
    val url = link.url ?: return
    if (URLUtil.isValidUrl(url)) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            logE("Failed to open profile link URL: $url", e)
        }
    } else {
        copyToClipboard(link.profileLinkSite?.name ?: "URL", url)
    }
}

/**
 * Encodes the avatar/cover URIs in [EditProfileViewModel.profileImageState] to Base64 data URLs
 * and returns a [ProfileImageContainer], or `null` if no images were selected.
 */
private fun createUserImageUpload(
    context: android.content.Context,
    viewModel: EditProfileViewModel
): ProfileImageContainer? {
    val imageState = viewModel.profileImageState
    val avatarUri = imageState.selectedAvatarUri
    val coverUri = imageState.selectedCoverUri
    if (avatarUri == null && coverUri == null) return null
    val profileImages = ProfileImageContainer(
        avatar = avatarUri?.let { readImageAsBase64(context, it) },
        coverImage = coverUri?.let { readImageAsBase64(context, it) }
    )
    if (profileImages.avatar == null && profileImages.coverImage == null) return null
    return profileImages
}

private fun readImageAsBase64(context: android.content.Context, uri: Uri): String? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
    return try {
        inputStream.use { stream ->
            Base64.encodeToString(stream.readBytes(), Base64.DEFAULT)
        }.let { base64 -> "data:$mimeType;base64,$base64" }
    } catch (e: Exception) {
        context.logE("Error encoding image to Base64 from uri: $uri", e)
        null
    }
}
