package io.github.drumber.kitsune.ui.profile.about

import androidx.navigation.fragment.findNavController
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toCharacterDto
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.profile.MyProfileFragmentDirections
import io.github.drumber.kitsune.ui.profile.MyProfileViewModel
import io.github.drumber.kitsune.ui.profile.follow.FollowListFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyProfileAboutFragment : BaseProfileAboutFragment() {
    override val viewModel: MyProfileViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onMediaClick(media: Media) {
        val action =
            MyProfileFragmentDirections.actionProfileFragmentToDetailsFragment(media.toMediaDto())
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }

    override fun openCharacterDetailsBottomSheet(character: Character) {
        val action =
            MyProfileFragmentDirections.actionProfileFragmentToCharacterDetailsBottomSheet(
                character.toCharacterDto()
            )
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }

    override fun navigateToFollowList(type: FollowListType) {
        val user = viewModel.getUser() ?: return
        val action = FollowListFragmentDirections.actionGlobalFollowListFragment(
            user.id,
            type,
            user.name
        )
        findNavController().navigateSafe(R.id.profile_fragment, action)
    }
}
