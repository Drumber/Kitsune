package io.github.drumber.kitsune.ui.navigation.graph

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.details.DetailsScreen
import io.github.drumber.kitsune.ui.details.DetailsViewModel
import io.github.drumber.kitsune.ui.details.LibraryChangeResult
import io.github.drumber.kitsune.ui.details.ManageLibraryScreen
import io.github.drumber.kitsune.ui.details.MediaMappingsScreen
import io.github.drumber.kitsune.ui.details.characters.CharacterDetailsSheet
import io.github.drumber.kitsune.ui.details.characters.CharactersScreen
import io.github.drumber.kitsune.ui.details.characters.CharactersViewModel
import io.github.drumber.kitsune.ui.details.episodes.EpisodesScreen
import io.github.drumber.kitsune.ui.details.episodes.EpisodesViewModel
import io.github.drumber.kitsune.ui.details.episodes.MediaUnitDetailsScreen
import io.github.drumber.kitsune.ui.details.feed.MediaFeedScreen
import io.github.drumber.kitsune.ui.details.feed.MediaFeedViewModel
import io.github.drumber.kitsune.ui.details.reactions.ReactionsScreen
import io.github.drumber.kitsune.ui.details.reactions.ReactionsViewModel
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.navigation.navigateSafe
import io.github.drumber.kitsune.ui.navigation.toMediaListRoute
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.detailsGraph(navController: NavHostController) {

    // ── Media Details ──────────────────────────────────────────────────────────
    composable<Routes.Details>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://kitsu.app/{type}/{slug}" },
            navDeepLink { uriPattern = "http://kitsu.app/{type}/{slug}" }
        )
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Details>()
        val viewModel: DetailsViewModel = koinViewModel()

        LaunchedEffect(route.mediaId, route.isAnime, route.type, route.slug) {
            when {
                route.mediaId != null && route.isAnime != null ->
                    viewModel.initMediaById(route.mediaId, route.isAnime)
                !route.type.isNullOrBlank() && !route.slug.isNullOrBlank() -> {
                    val isAnime = when (route.type!!.lowercase()) {
                        "anime" -> true
                        "manga" -> false
                        else -> null
                    }
                    if (isAnime != null) viewModel.initFromDeepLink(isAnime, route.slug!!)
                }
            }
        }

        val media by viewModel.mediaModel.collectAsStateWithLifecycle()
        val libraryEntry by viewModel.libraryEntryWrapper.collectAsStateWithLifecycle()
        val favorite by viewModel.favorite.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val reactions by viewModel.reactions.collectAsStateWithLifecycle(emptyList())
        val mappingsState by viewModel.mappingsSate.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val libraryUpdateNotFoundMessage = stringResource(R.string.error_library_update_not_found)
        val libraryUpdateFailedMessage = stringResource(R.string.error_library_update_failed)
        val libraryAddFailedMessage = stringResource(R.string.error_library_add_failed)
        val libraryDeleteFailedMessage = stringResource(R.string.error_library_delete_failed)
        val errorSomethingWrongMessage = stringResource(R.string.error_something_wrong)
        val logInRequiredMessage = stringResource(R.string.info_log_in_required)
        val noInformation = stringResource(R.string.no_information)

        // Library-change results → snackbar
        LaunchedEffect(
            libraryUpdateNotFoundMessage,
            libraryUpdateFailedMessage,
            libraryAddFailedMessage,
            libraryDeleteFailedMessage
        ) {
            viewModel.libraryChangeResultFlow.collect { result ->
                val message = when (result) {
                    is LibraryChangeResult.LibraryUpdateResult -> when (val r = result.result) {
                        is LibraryEntryUpdateResult.Failure -> when (r.reason) {
                            LibraryEntryUpdateFailureReason.NotFound -> libraryUpdateNotFoundMessage
                            else -> libraryUpdateFailedMessage
                        }
                        else -> null
                    }
                    LibraryChangeResult.AddNewLibraryEntryFailed -> libraryAddFailedMessage
                    LibraryChangeResult.DeleteLibraryEntryFailed -> libraryDeleteFailedMessage
                }
                if (message != null) snackbarHostState.showSnackbar(message)
            }
        }

        var showManageLibrary by remember { mutableStateOf(false) }
        var showMappings by remember { mutableStateOf(false) }
        var showReactionDialog by remember { mutableStateOf(false) }

        Box {
            DetailsScreen(
                media = media,
                libraryEntry = libraryEntry,
                favorite = favorite,
                reactions = reactions ?: emptyList(),
                isLoading = isLoading == true,
                isLoggedIn = viewModel.isLoggedIn(),
                onNavigateUp = { navController.navigateUp() },
                onShareMedia = {
                    val url = media?.let { m ->
                        val prefix = if (m is Anime) Kitsu.ANIME_URL_PREFIX else Kitsu.MANGA_URL_PREFIX
                        prefix + m.slug
                    }
                    if (url != null) {
                        val shareIntent = Intent.createChooser(
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, url)
                                type = "text/plain"
                            }, null
                        )
                        context.startActivity(shareIntent)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(errorSomethingWrongMessage)
                        }
                    }
                },
                onToggleFavorite = {
                    if (viewModel.isLoggedIn()) {
                        viewModel.toggleFavorite()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(logInRequiredMessage)
                        }
                    }
                },
                onOpenExternal = {
                    viewModel.loadMappingsIfNotAlreadyLoaded()
                    showMappings = true
                },
                onManageLibrary = {
                    if (viewModel.isLoggedIn()) {
                        showManageLibrary = true
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(logInRequiredMessage)
                        }
                    }
                },
                onEditLibraryEntry = {
                    if (viewModel.isLoggedIn()) {
                        val entryId = libraryEntry?.libraryEntry?.id
                        if (entryId != null) {
                            navController.navigateSafe(Routes.LibraryEditEntry(entryId))
                        }
                    }
                },
                onNavigateToEpisodes = {
                    media?.let { m ->
                        navController.navigateSafe(
                            Routes.Episodes(m.id, m is Anime, m.posterImageUrl)
                        )
                    }
                },
                onNavigateToCharacters = {
                    media?.let { m -> navController.navigateSafe(Routes.Characters(m.id, m is Anime)) }
                },
                onNavigateToFeed = {
                    media?.let { m -> navController.navigateSafe(Routes.MediaFeed(m.id, m is Anime)) }
                },
                onNavigateToReactions = {
                    media?.let { m -> navController.navigateSafe(Routes.Reactions(m.id, m is Anime)) }
                },
                onNavigateToCategory = { category ->
                    val slug = category.slug
                    val title = category.title ?: noInformation
                    if (slug != null) {
                        val mediaType = if (media is Anime) MediaType.Anime else MediaType.Manga
                        val selector = MediaSelector(
                            mediaType,
                            Filter().filter("categories", slug)
                                .sort(SortFilter.POPULARITY_DESC.queryParam).options
                        )
                        navController.navigateSafe(selector.toMediaListRoute(title))
                    }
                },
                onNavigateToFranchise = { franchiseMedia ->
                    navController.navigateSafe(
                        Routes.Details(
                            mediaId = franchiseMedia.id,
                            isAnime = franchiseMedia is Anime
                        )
                    )
                },
                onUpvoteReaction = { viewModel.upvoteReaction(it) },
                onAddReaction = { showReactionDialog = true },
                onCoverClick = {},
                onPosterClick = {},
                onOpenStreamingLink = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Manage Library bottom sheet
        if (showManageLibrary && media != null) {
            ModalBottomSheet(onDismissRequest = { showManageLibrary = false }) {
                ManageLibraryScreen(
                    title = media?.title,
                    isAnime = media is Anime,
                    existsInLibrary = libraryEntry != null,
                    onStatusClick = { status ->
                        viewModel.updateLibraryEntryStatus(status)
                        showManageLibrary = false
                    },
                    onRemoveClick = {
                        viewModel.removeLibraryEntry()
                        showManageLibrary = false
                    }
                )
            }
        }

        // Media Mappings (external links) bottom sheet
        if (showMappings) {
            ModalBottomSheet(onDismissRequest = { showMappings = false }) {
                MediaMappingsScreen(
                    state = mappingsState,
                    onOpenUrl = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
        }

        // Compose reaction dialog
        if (showReactionDialog) {
            var reactionText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showReactionDialog = false },
                title = { Text(stringResource(R.string.reaction_compose_title)) },
                text = {
                    OutlinedTextField(
                        value = reactionText,
                        onValueChange = { reactionText = it },
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val text = reactionText.trim()
                        if (text.isNotEmpty()) viewModel.createReaction(text)
                        showReactionDialog = false
                    }) {
                        Text(stringResource(R.string.reaction_compose_action_post))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReactionDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
    }

    // ── Episodes / Chapters ────────────────────────────────────────────────────
    composable<Routes.Episodes> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Episodes>()
        val viewModel: EpisodesViewModel = koinViewModel()

        LaunchedEffect(route.mediaId, route.isAnime) {
            viewModel.setMediaById(route.mediaId, route.isAnime)
        }

        var selectedMediaUnit by remember { mutableStateOf<MediaUnit?>(null) }

        val items = viewModel.dataSource.collectAsLazyPagingItems()
        val libraryEntry by viewModel.libraryEntryWrapper.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val libraryUpdateNotFoundMessage = stringResource(R.string.error_library_update_not_found)
        val libraryUpdateFailedMessage = stringResource(R.string.error_library_update_failed)

        LaunchedEffect(libraryUpdateNotFoundMessage, libraryUpdateFailedMessage) {
            viewModel.libraryUpdateResultFlow.collect { result ->
                if (result is LibraryEntryUpdateResult.Failure) {
                    val message = when (result.reason) {
                        LibraryEntryUpdateFailureReason.NotFound -> libraryUpdateNotFoundMessage
                        else -> libraryUpdateFailedMessage
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }

        val title = if (route.isAnime) {
            stringResource(R.string.title_episodes)
        } else {
            stringResource(R.string.title_chapters)
        }
        val posterUrl = route.posterUrl ?: libraryEntry?.media?.posterImageUrl

        Box {
            EpisodesScreen(
                title = title,
                items = items,
                posterUrl = posterUrl,
                isWatchCheckboxEnabled = libraryEntry != null,
                numberWatched = libraryEntry?.progress ?: 0,
                onNavigateUp = { navController.navigateUp() },
                onItemClick = { mediaUnit -> selectedMediaUnit = mediaUnit },
                onWatchedChanged = { mediaUnit, isWatched ->
                    viewModel.setMediaUnitWatched(mediaUnit, isWatched)
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        selectedMediaUnit?.let { mediaUnit ->
            val thumbnailUrl = mediaUnit.thumbnail?.smallOrHigher() ?: posterUrl
            ModalBottomSheet(onDismissRequest = { selectedMediaUnit = null }) {
                MediaUnitDetailsScreen(mediaUnit = mediaUnit, thumbnailUrl = thumbnailUrl)
            }
        }
    }

    // ── Characters ─────────────────────────────────────────────────────────────
    composable<Routes.Characters> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Characters>()
        val viewModel: CharactersViewModel = koinViewModel()

        LaunchedEffect(route.mediaId, route.isAnime) {
            viewModel.setMediaId(route.mediaId, route.isAnime)
        }

        var selectedCharacterId by remember { mutableStateOf<String?>(null) }

        val items = viewModel.dataSource.collectAsLazyPagingItems()
        val languages by viewModel.languages.collectAsStateWithLifecycle()

        CharactersScreen(
            title = stringResource(R.string.title_characters),
            items = items,
            languages = languages.orEmpty(),
            selectedLanguage = viewModel.selectedLanguage,
            onNavigateUp = { navController.navigateUp() },
            onLanguageSelected = { viewModel.setLanguage(it) },
            onCharacterClick = { character -> selectedCharacterId = character.id }
        )

        selectedCharacterId?.let { characterId ->
            CharacterDetailsSheet(
                characterId = characterId,
                onDismiss = { selectedCharacterId = null },
                onNavigateToMedia = { mediaId, isAnime ->
                    selectedCharacterId = null
                    navController.navigateSafe(Routes.Details(mediaId = mediaId, isAnime = isAnime))
                },
                onOpenPhoto = { imageUrl, title -> navController.navigateSafe(Routes.PhotoView(imageUrl, title)) }
            )
        }
    }

    // ── Media Feed ─────────────────────────────────────────────────────────────
    composable<Routes.MediaFeed> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.MediaFeed>()
        val viewModel: MediaFeedViewModel = koinViewModel()

        LaunchedEffect(route.mediaId, route.isAnime) {
            viewModel.setMedia(route.mediaId, route.isAnime)
        }

        val items = viewModel.dataSource.collectAsLazyPagingItems()

        MediaFeedScreen(
            title = stringResource(R.string.title_posts),
            items = items,
            onNavigateUp = { navController.navigateUp() },
            onPostClick = { post ->
                navController.navigateSafe(Routes.WebView("${Kitsu.BASE_URL}/posts/${post.id}"))
            },
            onAuthorClick = { userId ->
                navController.navigateSafe(Routes.UserProfile(userId))
            }
        )
    }

    // ── Reactions ──────────────────────────────────────────────────────────────
    composable<Routes.Reactions> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Reactions>()
        val viewModel: ReactionsViewModel = koinViewModel()

        LaunchedEffect(route.mediaId, route.isAnime) {
            viewModel.setMedia(route.mediaId, route.isAnime)
        }

        var showAddDialog by remember { mutableStateOf(false) }
        var editingReaction by remember { mutableStateOf<MediaReaction?>(null) }
        var deletingReaction by remember { mutableStateOf<MediaReaction?>(null) }
        var editDialogText by remember { mutableStateOf("") }

        LaunchedEffect(editingReaction) {
            editDialogText = editingReaction?.let { r ->
                r.reaction?.takeUnless { it.isBlank() } ?: r.content ?: ""
            } ?: ""
        }

        val items = viewModel.dataSource.collectAsLazyPagingItems()

        ReactionsScreen(
            title = stringResource(R.string.title_reactions),
            items = items,
            currentUserId = viewModel.currentUserId,
            onNavigateUp = { navController.navigateUp() },
            onAddReactionClick = { showAddDialog = true },
            onUpvoteClick = { viewModel.upvote(it) },
            onEditClick = { reaction -> editingReaction = reaction },
            onDeleteClick = { reaction -> deletingReaction = reaction }
        )

        // Add reaction dialog
        if (showAddDialog) {
            var addText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(stringResource(R.string.reaction_compose_title)) },
                text = {
                    OutlinedTextField(
                        value = addText,
                        onValueChange = { addText = it },
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val text = addText.trim()
                        if (text.isNotEmpty()) viewModel.createReaction(text)
                        showAddDialog = false
                    }) {
                        Text(stringResource(R.string.reaction_compose_action_post))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }

        // Edit reaction dialog
        editingReaction?.let {
            AlertDialog(
                onDismissRequest = { editingReaction = null },
                title = { Text(stringResource(R.string.reaction_compose_edit_title)) },
                text = {
                    OutlinedTextField(
                        value = editDialogText,
                        onValueChange = { editDialogText = it },
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val text = editDialogText.trim()
                        if (text.isNotEmpty()) viewModel.updateReaction(it, text)
                        editingReaction = null
                    }) {
                        Text(stringResource(R.string.action_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingReaction = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }

        // Delete confirmation dialog
        deletingReaction?.let { reaction ->
            AlertDialog(
                onDismissRequest = { deletingReaction = null },
                title = { Text(stringResource(R.string.delete_reaction_confirm_title)) },
                text = { Text(stringResource(R.string.delete_reaction_confirm_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteReaction(reaction)
                        deletingReaction = null
                    }) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingReaction = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
    }
}
