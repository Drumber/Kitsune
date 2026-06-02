package io.github.drumber.kitsune.ui.postdetail

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
import io.github.drumber.kitsune.databinding.FragmentPostDetailBinding
import io.github.drumber.kitsune.ui.adapter.paging.CommentPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.PostContentRenderer
import io.github.drumber.kitsune.util.ui.showSnackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private val args: PostDetailFragmentArgs by navArgs()

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModel()

    private val contentRenderer: PostContentRenderer by inject()

    private var replyTarget: io.github.drumber.kitsune.data.presentation.model.comment.Comment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostDetailBinding.bind(view)

        viewModel.setPost(args.post)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.layoutInput.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        val glide = Glide.with(this)
        val headerAdapter = PostDetailHeaderAdapter(
            glide = glide,
            onLikeClick = { viewModel.togglePostLike() },
            contentRenderer = contentRenderer,
            nsfwAllowed = viewModel.nsfwAllowed,
            onRevealClick = { viewModel.revealCurrentPost() },
            onMediaClick = { post -> openMedia(post) }
        )
        headerAdapter.setPost(args.post)

        val commentsAdapter = CommentPagingAdapter(
            glide = glide,
            onLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            contentRenderer = contentRenderer,
            scope = viewLifecycleOwner.lifecycleScope,
            repliesProvider = { comment -> viewModel.loadReplies(comment) },
            onReplyClick = { comment -> startReply(comment) }
        )

        binding.rvComments.apply {
            adapter = ConcatAdapter(
                headerAdapter,
                commentsAdapter.withLoadStateFooter(ResourceLoadStateAdapter(commentsAdapter))
            )
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }

        binding.btnSend.setOnClickListener { submitComment() }
        binding.btnCancelReply.setOnClickListener { cancelReply() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.postLikeState.collectLatest { state ->
                    headerAdapter.setLikeState(state.isLiked, state.count)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.revealedPosts.collectLatest { ids ->
                    headerAdapter.setRevealed(args.post.id in ids)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.comments.collectLatest { data ->
                    commentsAdapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        PostDetailViewModel.Event.LoginRequired ->
                            showSnackbar(binding.root, R.string.comment_login_required)

                        PostDetailViewModel.Event.CommentPosted -> {
                            binding.etComment.text?.clear()
                            cancelReply()
                            hideKeyboard()
                            commentsAdapter.refresh()
                            showSnackbar(binding.root, R.string.comment_posted)
                        }

                        PostDetailViewModel.Event.Error ->
                            showSnackbar(binding.root, R.string.comment_action_failed)

                        is PostDetailViewModel.Event.CommentLikeChanged ->
                            commentsAdapter.setLikeState(event.commentId, event.isLiked, event.count)
                    }
                }
            }
        }
    }

    private fun submitComment() {
        val content = binding.etComment.text?.toString().orEmpty().trim()
        if (content.isEmpty()) return
        val target = replyTarget
        if (target != null) {
            viewModel.postReply(target.id, content)
        } else {
            viewModel.postComment(content)
        }
    }

    private fun openMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun startReply(comment: io.github.drumber.kitsune.data.presentation.model.comment.Comment) {
        replyTarget = comment
        val author = comment.authorName
            ?: getString(R.string.feed_unknown_user)
        binding.tvReplyContext.text = getString(R.string.comment_replying_to, author)
        binding.layoutReplyContext.visibility = View.VISIBLE
        binding.tilComment.hint = getString(R.string.comment_reply_hint)
        binding.etComment.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun cancelReply() {
        replyTarget = null
        binding.layoutReplyContext.visibility = View.GONE
        binding.tilComment.hint = getString(R.string.hint_add_comment)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etComment.clearFocus()
        imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
