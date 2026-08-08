package io.github.drumber.kitsune.ui.replies

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.FragmentRepliesBinding
import io.github.drumber.kitsune.ui.adapter.paging.CommentPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.PostContentRenderer
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RepliesFragment : Fragment(R.layout.fragment_replies) {

    private val args: RepliesFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentRepliesBinding::bind)

    private val viewModel: RepliesViewModel by viewModel {
        parametersOf(args.parentCommentId, args.postId)
    }

    private val contentRenderer: PostContentRenderer by inject()

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

        val glide = Glide.with(this)

        val repliesAdapter = CommentPagingAdapter(
            glide = glide,
            onLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            contentRenderer = contentRenderer,
            currentUserId = viewModel.currentUserId(),
            onAuthorClick = { userId -> navigateToUserProfile(userId) }
        )

        val parentCommentAdapter = RepliesParentCommentAdapter(
            glide = glide,
            contentRenderer = contentRenderer,
            onAuthorClicked = { userId -> navigateToUserProfile(userId) },
            onLikeClicked = { comment -> viewModel.toggleCommentLike(comment) }
        )

        binding.rvReplies.apply {
            adapter = ConcatAdapter(
                parentCommentAdapter,
                repliesAdapter.withLoadStateFooter(ResourceLoadStateAdapter(repliesAdapter)),
            )
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        binding.layoutLoading.btnRetry.setOnClickListener { repliesAdapter.retry() }
        binding.btnSend.setOnClickListener { submitReply() }

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

                        RepliesViewModel.Event.ReplyPosted -> {
                            binding.etReply.text?.clear()
                            hideKeyboard()
                            repliesAdapter.refresh()
                            showSnackbar(binding.root, R.string.comment_posted)
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

    private fun submitReply() {
        val content = binding.etReply.text?.toString().orEmpty().trim()
        if (content.isEmpty()) return
        viewModel.postReply(content)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etReply.clearFocus()
        imm.hideSoftInputFromWindow(binding.etReply.windowToken, 0)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.replies_fragment, action)
    }
}
