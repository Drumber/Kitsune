package io.github.drumber.kitsune.ui.details.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacter
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openCharacterOnMAL
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class CharacterDetailsBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: CharacterDetailsViewModel by viewModel()

    private val navArgs by navArgs<CharacterDetailsBottomSheetArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val character by viewModel.characterFlow.collectAsStateWithLifecycle(null)
        val favorite by viewModel.favoriteFlow.collectAsStateWithLifecycle(null)
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        CharacterDetailsScreen(
            character = character,
            isFavorite = favorite != null,
            isLoadingMediaCharacters = uiState.isLoadingMediaCharacters,
            hasMediaCharacters = uiState.hasMediaCharacters,
            mediaCharacters = character?.mediaCharacters?.sortedBy { it.role?.ordinal }.orEmpty(),
            onFavoriteClick = {
                viewModel.toggleFavorite()
                findNavController().previousBackStackEntry
                    ?.takeIf { it.destination.id == R.id.profile_fragment }
                    ?.savedStateHandle?.set("refreshFavorites", true)
            },
            onOpenOnMal = {
                viewModel.characterFlow.replayCache.lastOrNull()?.malId?.let { malId ->
                    openCharacterOnMAL(malId)
                }
            },
            onMediaCharacterClick = { media -> navigateToMedia(media) },
            onImageClick = {
                val c = viewModel.characterFlow.replayCache.lastOrNull() ?: return@CharacterDetailsScreen
                c.image?.originalOrDown()?.let { imageUrl ->
                    openPhotoViewActivity(imageUrl, c.name, c.image.smallOrHigher())
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initCharacter(navArgs.character.toCharacter())
    }

    private fun navigateToMedia(media: Media) {
        val action = CharacterDetailsBottomSheetDirections
            .actionCharacterDetailsBottomSheetToDetailsFragment(media.toMediaDto())
        findNavController().navigateSafe(R.id.characterDetailsBottomSheet, action)
    }
}
