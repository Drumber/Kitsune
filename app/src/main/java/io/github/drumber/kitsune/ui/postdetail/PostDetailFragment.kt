package io.github.drumber.kitsune.ui.postdetail

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
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.postdetail.compose.PostDetailScreen
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostDetailFragment : Fragment() {

    private val args: PostDetailFragmentArgs by navArgs()

    private val viewModel: PostDetailViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { Content() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setPost(args.post)
    }

    @Composable
    @Suppress("LongMethod")
    private fun Content() {
        val post by viewModel.postState.collectAsStateWithLifecycle()
        val postLikeState by viewModel.postLikeState.collectAsStateWithLifecycle()
        val revealedPosts by viewModel.revealedPosts.collectAsStateWithLifecycle(initialValue = emptySet())
        val composerMode by viewModel.composerMode.collectAsStateWithLifecycle()
        val comments = viewModel.comments.collectAsLazyPagingItems()
        var commentLikeOverrides by remember { mutableStateOf<Map<String, Pair<Boolean, Int>>>(emptyMap()) }
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    PostDetailViewModel.Event.LoginRequired ->
                        snackbarMessage = getString(R.string.comment_login_required)
                    PostDetailViewModel.Event.Error ->
                        snackbarMessage = getString(R.string.comment_action_failed)
                    PostDetailViewModel.Event.CommentPosted -> {
                        viewModel.cancelComposer()
                        comments.refresh()
                        snackbarMessage = getString(R.string.comment_posted)
                    }
                    PostDetailViewModel.Event.CommentUpdated -> {
                        viewModel.cancelComposer()
                        comments.refresh()
                        snackbarMessage = getString(R.string.comment_updated)
                    }
                    PostDetailViewModel.Event.CommentDeleted -> {
                        comments.refresh()
                        snackbarMessage = getString(R.string.comment_deleted)
                    }
                    PostDetailViewModel.Event.PostDeleted -> {
                        snackbarMessage = getString(R.string.post_deleted)
                        findNavController().navigateUp()
                    }
                    is PostDetailViewModel.Event.CommentLikeChanged ->
                        commentLikeOverrides = commentLikeOverrides +
                            (event.commentId to Pair(event.isLiked, event.count))
                }
            }
        }

        PostDetailScreen(
            post = post,
            postLikeState = postLikeState,
            isPostRevealed = (post?.id ?: args.post.id) in revealedPosts,
            nsfwAllowed = viewModel.nsfwAllowed,
            comments = comments,
            commentLikeOverrides = commentLikeOverrides,
            composerMode = composerMode,
            currentUserId = viewModel.currentUserId(),
            snackbarMessage = snackbarMessage,
            onSnackbarShown = { snackbarMessage = null },
            onNavigateUp = { findNavController().navigateUp() },
            onPostLikeClick = { viewModel.togglePostLike() },
            onRevealPost = { viewModel.revealCurrentPost() },
            onMediaClick = { p -> navigateToMedia(p) },
            onEditPost = { p -> navigateToEditPost(p) },
            onDeletePost = { viewModel.deletePost() },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onCommentLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            onReplyClick = { comment -> viewModel.startReply(comment) },
            onViewAllRepliesClick = { comment -> navigateToReplies(comment) },
            onEditComment = { comment -> viewModel.startEditComment(comment) },
            onDeleteComment = { comment -> viewModel.deleteComment(comment.id) },
            onCancelComposer = { viewModel.cancelComposer() },
            onSubmitComment = { content -> submitComment(content) }
        )
    }

    private fun submitComment(content: String) {
        when (val mode = viewModel.composerMode.value) {
            is PostDetailViewModel.ComposerMode.Edit -> viewModel.updateComment(mode.comment.id, content)
            is PostDetailViewModel.ComposerMode.Reply -> viewModel.postReply(mode.comment.id, content)
            PostDetailViewModel.ComposerMode.Normal -> viewModel.postComment(content)
        }
    }

    private fun navigateToEditPost(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun navigateToMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun navigateToReplies(comment: Comment) {
        val action = PostDetailFragmentDirections.actionGlobalRepliesFragment(
            comment.id,
            args.post.id
        )
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }
}
