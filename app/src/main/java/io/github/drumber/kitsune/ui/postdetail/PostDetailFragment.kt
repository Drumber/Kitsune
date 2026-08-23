package io.github.drumber.kitsune.ui.postdetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionSet
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.motion.MotionUtils
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import io.github.drumber.kitsune.databinding.FragmentPostDetailBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.adapter.paging.CommentPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.report.ReportBottomSheet
import io.github.drumber.kitsune.util.extensions.getColor
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
import org.koin.core.qualifier.named

class PostDetailFragment : Fragment(R.layout.fragment_post_detail),
    NavigationBarView.OnItemReselectedListener {

    companion object {
        private const val KEY_LAST_NAV_DESTINATION = "last_nav_destination"
    }

    private val args: PostDetailFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentPostDetailBinding::bind)

    private val viewModel: PostDetailViewModel by viewModel()

    private var lastNavDestination: Int? = null

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())
    private val contentRenderer: PostContentRenderer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.containsKey(KEY_LAST_NAV_DESTINATION) == true) {
            lastNavDestination = savedInstanceState.getInt(KEY_LAST_NAV_DESTINATION)
        }

        lastNavDestination?.let { applyTransitions(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        // predictive back does not work shared element transition
        val disablePredictiveBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, disablePredictiveBackCallback)

         val transitionDuration = resources.getInteger(R.integer.material_motion_duration_short_2).toLong()
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.root_layout
            duration = transitionDuration
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().theme.getColor(R.attr.colorSurface))
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = args.sharedElementParentView.takeIf { it != -1 } ?: R.id.nav_host_fragment
            duration = transitionDuration
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().theme.getColor(R.attr.colorSurface))
        }

        val slideBottomInputLayout = Slide(Gravity.BOTTOM).apply {
            interpolator = MotionUtils.resolveThemeInterpolator(
                requireContext(),
                R.attr.motionEasingEmphasizedDecelerateInterpolator,
                FastOutSlowInInterpolator()
            )
            addTarget(R.id.layout_bottom_wrapper)
            duration = transitionDuration
        }
        val slideToolbar = MaterialFade().apply {
            duration = transitionDuration
            addTarget(R.id.app_bar_layout)
        }
        enterTransition = TransitionSet().apply {
            addTransition(slideToolbar)
            addTransition(slideBottomInputLayout)
        }

        viewModel.setPost(args.post)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        updateToolbarMenu(args.post)
        binding.toolbar.setOnMenuItemClickListener { handleMenuItemClick(it) }
        binding.layoutInput.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        val currentUserId = viewModel.currentUserId()
        val headerAdapter = PostDetailHeaderAdapter(
            imageLoader = imageLoader,
            contentRenderer = contentRenderer,
            nsfwAllowed = viewModel.nsfwAllowed,
            onLikeClick = { viewModel.togglePostLike() },
            onRevealClick = { viewModel.revealCurrentPost() },
            onMediaClick = { post -> openMedia(post) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onImageClick = { imageUrl -> openPhotoViewActivity(imageUrl, useSocialImageLoader = true) },
        )
        headerAdapter.setPost(args.post)

        val commentsAdapter = CommentPagingAdapter(
            imageLoader = imageLoader,
            contentRenderer = contentRenderer,
            currentUserId = currentUserId,
            onLikeClick = { comment -> viewModel.toggleCommentLike(comment) },
            onReplyClick = { comment -> startReply(comment) },
            onViewAllRepliesClick = { comment -> navigateToReplies(comment) },
            onEditClick = { comment -> startEditComment(comment) },
            onDeleteClick = { comment -> confirmDeleteComment(comment) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
            onImageClick = { imageUrl -> openPhotoViewActivity(imageUrl, useSocialImageLoader = true) },
            onShareClick = { comment -> showShareMenu(comment) },
            onReportClick = { comment -> openReportBottomSheet(comment) }
        )

        val commentsFooter = ResourceLoadStateAdapter(commentsAdapter)
        binding.rvComments.apply {
            adapter = ConcatAdapter(headerAdapter, commentsAdapter, commentsFooter)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            itemAnimator = null
        }

        // Surface the initial comment load (spinner/error/retry) as a footer below the post so the
        // post stays visible; fall back to the append state for subsequent pages.
        commentsAdapter.addLoadStateListener { loadState ->
            val refresh = loadState.source.refresh
            commentsFooter.loadState =
                if (refresh is LoadState.Loading || refresh is LoadState.Error) {
                    refresh
                } else {
                    loadState.source.append
                }
        }

        binding.btnSend.setOnClickListener { submitComment() }
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
                viewModel.postState.collectLatest { post ->
                    post?.let {
                        headerAdapter.setPost(it)
                        updateToolbarMenu(it)
                    }
                }
            }
        }

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
                            showSnackbar(binding.rootLayout, R.string.comment_login_required)

                        PostDetailViewModel.Event.CommentPosted -> {
                            binding.etComment.text?.clear()
                            viewModel.cancelComposer()
                            hideKeyboard()
                            commentsAdapter.refresh()
                            showSnackbar(binding.rootLayout, R.string.comment_posted)
                        }

                        PostDetailViewModel.Event.Error ->
                            showSnackbar(binding.rootLayout, R.string.comment_action_failed)

                        PostDetailViewModel.Event.PostDeleted -> {
                            showSnackbar(binding.rootLayout, R.string.post_deleted)
                            findNavController().navigateUp()
                        }

                        PostDetailViewModel.Event.CommentUpdated -> {
                            binding.etComment.text?.clear()
                            viewModel.cancelComposer()
                            hideKeyboard()
                            commentsAdapter.refresh()
                            showSnackbar(binding.rootLayout, R.string.comment_updated)
                        }

                        PostDetailViewModel.Event.CommentDeleted -> {
                            commentsAdapter.refresh()
                            showSnackbar(binding.rootLayout, R.string.comment_deleted)
                        }

                        is PostDetailViewModel.Event.CommentLikeChanged ->
                            commentsAdapter.setLikeState(
                                event.commentId,
                                event.isLiked,
                                event.count
                            )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lastNavDestination?.let { outState.putInt(KEY_LAST_NAV_DESTINATION, it) }
    }

    private fun applyTransitions(destinationId: Int) {
        lastNavDestination = destinationId
        when (destinationId) {
            R.id.user_profile_fragment, R.id.replies_fragment -> {
                exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            }

            R.id.details_fragment -> {
                exitTransition = MaterialFadeThrough()
                reenterTransition = MaterialFadeThrough()
            }

            else -> {
                exitTransition = null
                reenterTransition = null
            }
        }
    }

    private fun submitComment() {
        val content = binding.etComment.text?.toString().orEmpty().trim()
        if (content.isEmpty()) return
        when (val mode = viewModel.composerMode.value) {
            is PostDetailViewModel.ComposerMode.Edit ->
                viewModel.updateComment(mode.comment.id, content)

            is PostDetailViewModel.ComposerMode.Reply ->
                viewModel.postReply(mode.comment.id, content)

            PostDetailViewModel.ComposerMode.Normal ->
                viewModel.postComment(content)
        }
    }

    private fun navigateToEditPost(post: Post) {
        val action = PostDetailFragmentDirections.actionGlobalCreatePostFragment(post)
        applyTransitions(R.id.create_post_fragment)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(userId)
        applyTransitions(R.id.user_profile_fragment)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun navigateToReplies(comment: Comment) {
        val action =
            PostDetailFragmentDirections.actionGlobalRepliesFragment(comment.id, args.post.id)
        applyTransitions(R.id.replies_fragment)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun confirmDeletePost() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_post_confirm_title)
            .setMessage(R.string.delete_post_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deletePost() }
            .show()
    }

    private fun confirmDeleteComment(comment: Comment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_comment_confirm_title)
            .setMessage(R.string.delete_comment_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteComment(comment.id) }
            .show()
    }

    private fun openMedia(post: Post) {
        val slug = post.mediaSlug
        val isAnime = post.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        applyTransitions(R.id.details_fragment)
        findNavController().navigateSafe(R.id.post_detail_fragment, action)
    }

    private fun startReply(comment: Comment) {
        viewModel.startReply(comment)
        focusCommentInput()
    }

    private fun startEditComment(comment: Comment) {
        viewModel.startEditComment(comment)
        binding.etComment.setText(comment.content)
        binding.etComment.setSelection(binding.etComment.text?.length ?: 0)
        focusCommentInput()
    }

    private fun renderComposerMode(mode: PostDetailViewModel.ComposerMode) {
        when (mode) {
            PostDetailViewModel.ComposerMode.Normal -> {
                binding.layoutReplyContext.visibility = View.GONE
                binding.tilComment.hint = getString(R.string.hint_add_comment)
            }

            is PostDetailViewModel.ComposerMode.Reply -> {
                val author = mode.comment.authorName
                    ?: getString(R.string.feed_unknown_user)
                binding.tvReplyContext.text = getString(R.string.comment_replying_to, author)
                binding.layoutReplyContext.visibility = View.VISIBLE
                binding.tilComment.hint = getString(R.string.comment_reply_hint)
            }

            is PostDetailViewModel.ComposerMode.Edit -> {
                binding.tvReplyContext.text = getString(R.string.comment_editing)
                binding.layoutReplyContext.visibility = View.VISIBLE
                binding.tilComment.hint = getString(R.string.hint_add_comment)
            }
        }
    }

    private fun cancelComposer() {
        val wasEditing = viewModel.composerMode.value is PostDetailViewModel.ComposerMode.Edit
        viewModel.cancelComposer()
        if (wasEditing) {
            binding.etComment.text?.clear()
        }
    }

    private fun focusCommentInput() {
        binding.etComment.requestFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etComment.clearFocus()
        imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
    }

    private fun updateToolbarMenu(post: Post) {
        val localUserId = viewModel.currentUserId()
        val isOwner = localUserId != null && post.authorId == localUserId
        val menu = binding.toolbar.menu

        menu.findItem(R.id.action_edit_item)?.isVisible = isOwner
        menu.findItem(R.id.action_delete_item)?.isVisible = isOwner
        menu.findItem(R.id.action_report_item)?.isVisible = !isOwner && localUserId != null
    }

    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        val post = viewModel.postState.value ?: return false
        return when (menuItem.itemId) {
            R.id.action_share_item -> {
                showShareMenu(post)
                true
            }

            R.id.action_edit_item -> {
                navigateToEditPost(post)
                true
            }

            R.id.action_delete_item -> {
                confirmDeletePost()
                true
            }

            R.id.action_report_item -> {
                openReportBottomSheet(post)
                true
            }

            else -> false
        }
    }

    private fun showShareMenu(comment: Comment) {
        startUrlShareIntent("${Kitsu.BASE_URL}/comments/${comment.id}")
    }

    private fun showShareMenu(post: Post) {
        startUrlShareIntent("${Kitsu.BASE_URL}/posts/${post.id}")
    }

    private fun openReportBottomSheet(post: Post) {
        ReportBottomSheet.create(post.id, ReportTarget.POST)
            .show(childFragmentManager, ReportBottomSheet.TAG)
    }

    private fun openReportBottomSheet(comment: Comment) {
        ReportBottomSheet.create(comment.id, ReportTarget.COMMENT)
            .show(childFragmentManager, ReportBottomSheet.TAG)
    }

    override fun onNavigationItemReselected(p0: MenuItem) {
        if (binding.rvComments.canScrollVertically(-1)) {
            binding.rvComments.smoothScrollOrJumpToTop()
        } else {
            findNavController().navigateUp()
        }
    }
}
