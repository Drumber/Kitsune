package io.github.drumber.kitsune.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Notification
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.postdetail.PostDetailFragmentDirections
import io.github.drumber.kitsune.ui.reactiondetail.ReactionDetailFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.compose.koinViewModel

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        NotificationsContent()
    }

    @Composable
    private fun NotificationsContent() {
        val viewModel: NotificationsViewModel = koinViewModel()
        val notifications = viewModel.notifications.collectAsLazyPagingItems()

        NotificationsScreen(
            notifications = notifications,
            loginRequired = viewModel.loginRequired,
            onNavigateUp = { findNavController().navigateUp() },
            onNotificationClick = { notification -> navigateToTarget(notification) }
        )
    }

    private fun navigateToTarget(notification: Notification) {
        val reactionId = notification.targetReactionId
        if (reactionId != null) {
            val action = ReactionDetailFragmentDirections
                .actionGlobalReactionDetailFragment(reactionId)
            findNavController().navigateSafe(R.id.notifications_fragment, action)
            return
        }
        val post = notification.targetPost ?: return
        val action = PostDetailFragmentDirections.actionGlobalPostDetailFragment(post)
        findNavController().navigateSafe(R.id.notifications_fragment, action)
    }
}
