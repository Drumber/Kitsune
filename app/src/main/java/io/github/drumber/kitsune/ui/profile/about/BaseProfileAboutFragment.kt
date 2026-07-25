package io.github.drumber.kitsune.ui.profile.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.profile.BaseProfileViewModel
import io.github.drumber.kitsune.ui.profile.UserProfileUiState
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.openUrl

abstract class BaseProfileAboutFragment : BaseFragment(R.layout.fragment_profile_about),
    NavigationBarView.OnItemReselectedListener {

    protected abstract val viewModel: BaseProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val user by viewModel.userModel.collectAsStateWithLifecycle()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProfileAboutScreen(
            user = user,
            isRefreshing = uiState.isRefreshing,
            isInitialLoading = uiState.isInitialLoading,
            followState = provideFollowState(),
            onRefresh = viewModel::refreshUser,
            onFollowClick = ::onFollowClick,
            onFollowingClick = { navigateToFollowList(FollowListType.FOLLOWING) },
            onFollowersClick = { navigateToFollowList(FollowListType.FOLLOWERS) },
            onWaifuClick = ::openCharacterDetailsBottomSheet,
            onMediaClick = ::onMediaClick,
            onCharacterClick = ::openCharacterDetailsBottomSheet,
            onProfileLinkClick = ::onProfileLinkClicked
        )
    }

    @Composable
    protected open fun provideFollowState(): UserProfileUiState? = null

    protected open fun onFollowClick() = Unit

    protected abstract fun onMediaClick(media: Media)

    protected abstract fun openCharacterDetailsBottomSheet(character: Character)

    protected abstract fun navigateToFollowList(type: FollowListType)

    private fun onProfileLinkClicked(profileLink: ProfileLink) {
        profileLink.url?.let { url ->
            if (URLUtil.isValidUrl(url)) {
                openUrl(url)
            } else {
                copyToClipboard(profileLink.profileLinkSite?.name ?: "URL", url)
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) = Unit
}
