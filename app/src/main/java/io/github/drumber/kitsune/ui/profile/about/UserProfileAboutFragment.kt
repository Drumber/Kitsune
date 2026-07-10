package io.github.drumber.kitsune.ui.profile.about

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileViewModel
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserProfileAboutFragment : BaseProfileAboutFragment() {

    override val viewModel: UserProfileViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.btnFollow.apply {
                    isVisible = state.canFollow
                    isEnabled = !state.isFollowProcessing
                    setText(
                        if (state.isFollowing) R.string.action_unfollow
                        else R.string.action_follow
                    )
                }
            }
        }
    }

    override fun onFavoriteMediaItemClicked(view: View, media: Media) {
        val action = UserProfileFragmentDirections.actionGlobalDetailsFragment(
            media = media.toMediaDto()
        )
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.user_profile_fragment, action, extras)
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
