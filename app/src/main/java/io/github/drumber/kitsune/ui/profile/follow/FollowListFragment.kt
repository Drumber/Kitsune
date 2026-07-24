package io.github.drumber.kitsune.ui.profile.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class FollowListFragment : Fragment() {

    private val args: FollowListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        FollowListContent()
    }

    @Composable
    private fun FollowListContent() {
        val viewModel: FollowListViewModel = koinViewModel(
            parameters = { parametersOf(args.userId, args.listType) }
        )
        val users = viewModel.users.collectAsLazyPagingItems()
        val followStates by viewModel.followStates.collectAsStateWithLifecycle()
        val title = when (args.listType) {
            FollowListType.FOLLOWING -> stringResource(R.string.follow_list_following_title)
            FollowListType.FOLLOWERS -> stringResource(R.string.follow_list_followers_title)
        }
        FollowListScreen(
            title = title,
            users = users,
            followStates = followStates,
            onNavigateUp = { findNavController().navigateUp() },
            onUserClick = ::navigateToUser,
            onFollowClick = { viewModel.toggleFollow(it) },
            onResolveFollowState = { viewModel.resolveFollowState(it) },
            showButtonFor = { viewModel.showButtonFor(it) }
        )
    }

    private fun navigateToUser(userId: String) {
        val action = UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId, null)
        findNavController().navigateSafe(R.id.follow_list_fragment, action)
    }
}
