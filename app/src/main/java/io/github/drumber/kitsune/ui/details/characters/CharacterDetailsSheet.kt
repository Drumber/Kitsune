package io.github.drumber.kitsune.ui.details.characters

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import org.koin.androidx.compose.koinViewModel

/**
 * Reusable bottom sheet that shows full character details.
 *
 * Hosts [CharacterDetailsScreen] inside a Material 3 [ModalBottomSheet] driven by local state.
 * The caller is responsible for dismissal logic and routing side-effects; this composable is
 * intentionally free of any NavController dependency.
 *
 * @param characterId    The Kitsu character id to load.
 * @param onDismiss      Called when the sheet should be dismissed (user swipe or back press).
 * @param onNavigateToMedia  Called when the user taps a media appearance; provides the mediaId
 *                       and a flag indicating whether it is an anime (vs. manga).
 * @param onFavoriteChanged  Called after the user toggles the character's favourite state;
 *                       useful for refreshing parent screens (e.g. MyProfile).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsSheet(
    characterId: String,
    onDismiss: () -> Unit,
    onNavigateToMedia: (mediaId: String, isAnime: Boolean) -> Unit,
    onOpenPhoto: (imageUrl: String, title: String?) -> Unit,
    onFavoriteChanged: () -> Unit = {}
) {
    val viewModel: CharacterDetailsViewModel = koinViewModel(key = characterId)
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(characterId) {
        viewModel.initCharacterById(characterId)
    }

    val character by viewModel.characterFlow.collectAsStateWithLifecycle(null)
    val favorite by viewModel.favoriteFlow.collectAsStateWithLifecycle(null)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        CharacterDetailsScreen(
            character = character,
            isFavorite = favorite != null,
            isLoadingMediaCharacters = uiState.isLoadingMediaCharacters,
            hasMediaCharacters = uiState.hasMediaCharacters,
            mediaCharacters = character?.mediaCharacters?.sortedBy { it.role?.ordinal }.orEmpty(),
            onFavoriteClick = {
                viewModel.toggleFavorite()
                onFavoriteChanged()
            },
            onOpenOnMal = {
                viewModel.characterFlow.replayCache.lastOrNull()?.malId?.let { malId ->
                    val url = "https://myanimelist.net/character/$malId"
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: Exception) { /* no browser installed */ }
                }
            },
            onMediaCharacterClick = { media ->
                onNavigateToMedia(media.id, media is Anime)
            },
            onImageClick = {
                val c = viewModel.characterFlow.replayCache.lastOrNull()
                val imageUrl = c?.image?.originalOrDown()
                if (c != null && imageUrl != null) {
                    onOpenPhoto(imageUrl, c.name)
                }
            }
        )
    }
}
