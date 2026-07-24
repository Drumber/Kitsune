package io.github.drumber.kitsune.ui.replies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.replies.compose.RepliesScreen
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RepliesFragment : Fragment() {

    private val args: RepliesFragmentArgs by navArgs()

    private val viewModel: RepliesViewModel by viewModel {
        parametersOf(args.parentCommentId, args.postId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { Content() }

    @Composable
    private fun Content() {
        val parentComment by viewModel.parentComment.collectAsStateWithLifecycle()
        val replies = viewModel.replies.collectAsLazyPagingItems()
        var parentIsLiked by remember { mutableStateOf(parentComment?.isLikedByMe ?: false) }
        var parentLikesCount by remember { mutableStateOf(parentComment?.likesCount ?: 0) }
        var commentLikeOverrides by remember { mutableStateOf<Map<String, Pair<Boolean, Int>>>(emptyMap()) }
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(parentComment) {
            parentComment?.let {
                parentIsLiked = it.isLikedByMe
                parentLikesCount = it.likesCount
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    is RepliesViewModel.Event.CommentLikeChanged ->
                        if (event.commentId == args.parentCommentId) {
                            parentIsLiked = event.isLiked
                            parentLikesCount = event.count
                        } else {
                            commentLikeOverrides = commentLikeOverrides +
                                (event.commentId to Pair(event.isLiked, event.count))
                        }
                    RepliesViewModel.Event.ReplyPosted -> {
                        replies.refresh()
                        snackbarMessage = getString(R.string.comment_posted)
                    }
                    RepliesViewModel.Event.LoginRequired ->
                        snackbarMessage = getString(R.string.comment_login_required)
                    RepliesViewModel.Event.Error ->
                        snackbarMessage = getString(R.string.comment_action_failed)
                }
            }
        }

        RepliesScreen(
            parentComment = parentComment,
            parentIsLiked = parentIsLiked,
            parentLikesCount = parentLikesCount,
            replies = replies,
            commentLikeOverrides = commentLikeOverrides,
            currentUserId = viewModel.currentUserId(),
            snackbarMessage = snackbarMessage,
            onSnackbarShown = { snackbarMessage = null },
            onNavigateUp = { findNavController().navigateUp() },
            onParentLikeClick = { parentComment?.let { viewModel.toggleCommentLike(it) } },
            onReplyLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onSubmitReply = { content -> viewModel.postReply(content) }
        )
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.replies_fragment, action)
    }
}
