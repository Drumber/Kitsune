package io.github.drumber.kitsune.ui.profile.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileUiState
import io.github.drumber.kitsune.ui.profile.UserProfileViewModel
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserProfileAboutFragment : BaseProfileAboutFragment() {

    override val viewModel: UserProfileViewModel by viewModel(ownerProducer = { requireParentFragment() })

    @Composable
    override fun provideFollowState(): UserProfileUiState {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        return state
    }

    override fun onFollowClick() {
        viewModel.toggleFollow()
    }

    override fun onMediaClick(media: Media) {
        val action = UserProfileFragmentDirections.actionGlobalDetailsFragment(
            media = media.toMediaDto()
        )
        findNavController().navigateSafe(R.id.user_profile_fragment, action)
    }

    override fun openCharacterDetailsBottomSheet(character: Character) {
        val action = UserProfileFragmentDirections
            .actionGlobalCharacterDetailsBottomSheet(character.toCharacterDto())
        findNavController().navigateSafe(R.id.user_profile_fragment, action)
    }

    override fun navigateToFollowList(type: FollowListType) {
        val user = viewModel.getUser() ?: return
        val action = FollowListFragmentDirections.actionGlobalFollowListFragment(
            user.id,
            type,
            user.name
        )
        findNavController().navigateSafe(R.id.user_profile_fragment, action)
    }
}
