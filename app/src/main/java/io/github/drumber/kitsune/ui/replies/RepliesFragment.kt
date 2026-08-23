package io.github.drumber.kitsune.ui.replies

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import io.github.drumber.kitsune.databinding.FragmentRepliesBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.adapter.paging.CommentPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.report.ReportBottomSheet
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.openPhotoViewActivity
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.markwon.PostContentRenderer
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class RepliesFragment : Fragment(R.layout.fragment_replies),
    NavigationBarView.OnItemReselectedListener {

    private val args: RepliesFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentRepliesBinding::bind)

    private val viewModel: RepliesViewModel by viewModel {
        parametersOf(args.parentCommentId, args.postId)
    }

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())
    private val contentRenderer: PostContentRenderer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.rvReplies.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )
        binding.layoutInput.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        val repliesAdapter = CommentPagingAdapter(
            imageLoader = imageLoader,
            contentRenderer = contentRenderer,
            currentUserId = viewModel.currentUserId(),
            onLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onImageClick = { imageUrl -> openPhotoViewActivity(imageUrl) },
            onShareClick = { comment -> showShareMenu(comment) },
            onReportClick = { comment -> openReportBottomSheet(comment) },
            onReplyClick = { comment -> startSecondLevelReply(comment) },
            onEditClick = { comment -> startEditComment(comment) },
            onDeleteClick = { comment -> confirmDeleteComment(comment) },
            onViewAllRepliesClick = null,
        )

        val parentCommentAdapter = RepliesParentCommentAdapter(
            imageLoader = imageLoader,
            contentRenderer = contentRenderer,
            currentUserId = viewModel.currentUserId(),
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            onEditClick = { comment -> startEditComment(comment) },
            onReportClick = { comment -> openReportBottomSheet(comment) },
            onDeleteClick = { comment -> confirmDeleteComment(comment) },
            onShareClick = { comment -> showShareMenu(comment) },
            onImageClick = { imageUrl -> openPhotoViewActivity(imageUrl) },
        )

        binding.rvReplies.apply {
            adapter = ConcatAdapter(
                parentCommentAdapter,
                repliesAdapter.withLoadStateFooter(ResourceLoadStateAdapter(repliesAdapter)),
            )
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            itemAnimator = null
        }

        binding.layoutLoading.btnRetry.setOnClickListener { repliesAdapter.retry() }
        binding.btnSend.setOnClickListener { submitReply() }
        binding.btnCancelReply.setOnClickListener { cancelComposer() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.composerMode.collectLatest { mode ->
                    renderComposerMode(mode)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.parentComment.collectLatest { comment ->
                    parentCommentAdapter.setComment(comment)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repliesAdapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvReplies,
                        repliesAdapter.itemCount,
                        loadState
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.replies.collectLatest { data ->
                    repliesAdapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is RepliesViewModel.Event.CommentLikeChanged ->
                            if (event.commentId == args.parentCommentId) {
                                parentCommentAdapter.setLikeState(event.commentId, event.isLiked, event.count)
                            } else {
                                repliesAdapter.setLikeState(
                                    event.commentId,
                                    event.isLiked,
                                    event.count
                                )
                            }

                        is RepliesViewModel.Event.CommentDeleted -> {
                            if (event.isParentComment) {
                                findNavController().navigateUp()
                            } else {
                                repliesAdapter.refresh()
                            }
                            showSnackbar(binding.rootLayout, R.string.comment_deleted)
                        }

                        RepliesViewModel.Event.ReplyPosted -> {
                            binding.etReply.text?.clear()
                            viewModel.cancelComposer()
                            hideKeyboard()
                            repliesAdapter.refresh()
                            showSnackbar(binding.root, R.string.comment_posted)
                        }

                        is RepliesViewModel.Event.ReplyUpdated -> {
                            binding.etReply.text?.clear()
                            viewModel.cancelComposer()
                            hideKeyboard()
                            showSnackbar(binding.root, R.string.comment_updated)
                            if (!event.isParentComment) {
                                repliesAdapter.refresh()
                            }
                        }

                        RepliesViewModel.Event.LoginRequired ->
                            showSnackbar(binding.root, R.string.comment_login_required)

                        RepliesViewModel.Event.Error ->
                            showSnackbar(binding.root, R.string.comment_action_failed)
                    }
                }
            }
        }
    }

    private fun renderComposerMode(mode: RepliesViewModel.ComposerMode) {
        when (mode) {
            RepliesViewModel.ComposerMode.Normal -> {
                binding.layoutReplyContext.isVisible = false
            }

            is RepliesViewModel.ComposerMode.Edit -> {
                binding.layoutReplyContext.isVisible = true
            }
        }
    }

    private fun cancelComposer() {
        viewModel.cancelComposer()
        binding.etReply.text?.clear()
    }

    private fun startSecondLevelReply(comment: Comment) {
        // second level nested replies are not supported; instead mention user by slug/id
        val existingContent = binding.etReply.text?.trim()?.let { existing ->
            // remove any existing user mention at the beginning of the text
            val userMentionRegex = Regex("^@[\\w._+-]+\\h?")
            existing.replaceFirst(userMentionRegex, "")
        }.orEmpty()
        val userHandle = comment.authorSlug ?: comment.authorId
        val newContent = "@$userHandle $existingContent"
        binding.etReply.setText(newContent)
        binding.etReply.setSelection(newContent.length)
        focusReplyInput()
    }

    private fun startEditComment(comment: Comment) {
        viewModel.startEditComment(comment)
        binding.etReply.setText(comment.content)
        binding.etReply.setSelection(comment.content?.length ?: 0)
        focusReplyInput()
    }

    private fun confirmDeleteComment(comment: Comment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_comment_confirm_title)
            .setMessage(R.string.delete_comment_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteComment(comment.id) }
            .show()
    }

    private fun submitReply() {
        val content = binding.etReply.text?.toString().orEmpty().trim()
        if (content.isEmpty()) return
        when (val mode = viewModel.composerMode.value) {
            is RepliesViewModel.ComposerMode.Edit ->
                viewModel.updateComment(mode.comment.id, content)

            RepliesViewModel.ComposerMode.Normal -> viewModel.postReply(content)
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etReply.clearFocus()
        imm.hideSoftInputFromWindow(binding.etReply.windowToken, 0)
    }

    private fun focusReplyInput() {
        binding.etReply.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etReply, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.replies_fragment, action)
    }

    private fun showShareMenu(comment: Comment) {
        startUrlShareIntent("${Kitsu.BASE_URL}/comments/${comment.id}")
    }

    private fun openReportBottomSheet(comment: Comment) {
        ReportBottomSheet.create(comment.id, ReportTarget.COMMENT)
            .show(childFragmentManager, ReportBottomSheet.TAG)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvReplies.canScrollVertically(-1)) {
            binding.rvReplies.smoothScrollOrJumpToTop()
        } else {
            findNavController().navigateUp()
        }
    }
}
