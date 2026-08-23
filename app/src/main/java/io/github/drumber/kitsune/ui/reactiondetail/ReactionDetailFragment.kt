package io.github.drumber.kitsune.ui.reactiondetail

import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.presentation.model.report.ReportTarget
import io.github.drumber.kitsune.databinding.DialogComposeReactionBinding
import io.github.drumber.kitsune.databinding.FragmentReactionDetailBinding
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.report.ReportBottomSheet
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReactionDetailFragment : Fragment(R.layout.fragment_reaction_detail) {

    private val args: ReactionDetailFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentReactionDetailBinding::bind)

    private val viewModel: ReactionDetailViewModel by viewModel {
        parametersOf(args.reactionId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.toolbar.setOnMenuItemClickListener { handleMenuItemClick(it) }
        binding.scrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        binding.btnUpvote.setOnClickListener { viewModel.upvote() }

        binding.tvAuthor.setOnClickListener { navigateToAuthorProfile() }
        binding.ivAvatar.setOnClickListener { navigateToAuthorProfile() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { loading ->
                    binding.scrollView.isVisible = !loading
                    binding.loadingIndicator.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reaction.collectLatest { reaction ->
                    reaction?.let {
                        bindReaction(it)
                        updateToolbarMenu(reaction)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isUpvoted.collectLatest { upvoted ->
                    binding.btnUpvote.isEnabled = !upvoted
                    binding.btnUpvote.setIconResource(
                        if (upvoted) R.drawable.ic_thumb_up_24 else R.drawable.ic_thumb_up_border_24
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is ReactionDetailViewModel.Event.UpvoteSuccess ->
                            binding.btnUpvote.text = event.newCount.toString()

                        ReactionDetailViewModel.Event.LoginRequired ->
                            showSnackbar(R.string.reactions_upvote_login_required)

                        ReactionDetailViewModel.Event.UpvoteFailed ->
                            showSnackbar(R.string.reactions_upvote_failed)

                        ReactionDetailViewModel.Event.UpdateSuccess ->
                            showSnackbar(R.string.reaction_updated)

                        ReactionDetailViewModel.Event.DeleteSuccess -> {
                            showSnackbar(R.string.reaction_deleted)
                            findNavController().navigateUp()
                        }

                        ReactionDetailViewModel.Event.UpdateFailed,
                        ReactionDetailViewModel.Event.DeleteFailed ->
                            showSnackbar(R.string.action_failed)
                    }
                }
            }
        }
    }

    private fun bindReaction(reaction: MediaReaction) {
        binding.ivAvatar.load(reaction.authorAvatarUrl) {
            placeholder(R.drawable.ic_outline_person_24)
            error(R.drawable.ic_outline_person_24)
            fallback(R.drawable.ic_outline_person_24)
        }

        binding.tvAuthor.text = reaction.authorName
            ?: getString(R.string.feed_unknown_user)

        binding.tvTimestamp.text = reaction.createdAt?.parseUtcDate()?.let { date ->
            DateUtils.getRelativeTimeSpanString(
                date.time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }
        binding.tvTimestamp.isVisible = !binding.tvTimestamp.text.isNullOrBlank()

        binding.tvReaction.text = reaction.reaction ?: reaction.content

        val hasMedia = !reaction.mediaTitle.isNullOrBlank()
        binding.cardMedia.isVisible = hasMedia
        if (hasMedia) {
            binding.tvMediaTitle.text = reaction.mediaTitle
            binding.ivMediaPoster.load(reaction.mediaPosterUrl)
            binding.cardMedia.setOnClickListener { openMedia(reaction) }
        }

        binding.btnUpvote.text = reaction.upVotesCount.toString()
    }

    private fun updateToolbarMenu(reaction: MediaReaction) {
        val localUserId = viewModel.currentUserId()
        val isOwner = localUserId != null && reaction.authorId == localUserId
        val menu = binding.toolbar.menu

        menu.findItem(R.id.action_edit_item)?.isVisible = isOwner
        menu.findItem(R.id.action_delete_item)?.isVisible = isOwner
        menu.findItem(R.id.action_report_item)?.isVisible = !isOwner && localUserId != null
    }

    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        val reaction = viewModel.reaction.value ?: return false
        return when (menuItem.itemId) {
            R.id.action_share_item -> {
                showShareMenu(reaction)
                true
            }

            R.id.action_edit_item -> {
                showEditReactionDialog(reaction)
                true
            }

            R.id.action_delete_item -> {
                confirmDeleteReaction(reaction)
                true
            }

            R.id.action_report_item -> {
                openReportBottomSheet(reaction)
                true
            }

            else -> false
        }
    }

    private fun openMedia(reaction: MediaReaction) {
        val slug = reaction.mediaSlug
        val isAnime = reaction.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.reaction_detail_fragment, action)
    }

    private fun showSnackbar(messageResId: Int) {
        Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToAuthorProfile() {
        val authorId = viewModel.reaction.value?.authorId ?: return
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(authorId)
        findNavController().navigateSafe(R.id.reaction_detail_fragment, action)
    }

    private fun showShareMenu(reaction: MediaReaction) {
        startUrlShareIntent("${Kitsu.BASE_URL}/media-reactions/${reaction.id}")
    }

    private fun openReportBottomSheet(reaction: MediaReaction) {
        ReportBottomSheet.create(reaction.id, ReportTarget.MEDIA_REACTION)
            .show(childFragmentManager, ReportBottomSheet.TAG)
    }

    private fun showEditReactionDialog(existing: MediaReaction) {
        val dialogBinding = DialogComposeReactionBinding.inflate(layoutInflater)
        val initialText = existing.reaction?.takeUnless { it.isBlank() } ?: existing.content
        dialogBinding.etReaction.setText(initialText)
        dialogBinding.etReaction.setSelection(dialogBinding.etReaction.text?.length ?: 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reaction_compose_edit_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val text = dialogBinding.etReaction.text?.toString().orEmpty().trim()
                if (text.isEmpty()) return@setPositiveButton
                viewModel.updateReaction(existing, text)
            }
            .show()
    }

    private fun confirmDeleteReaction(reaction: MediaReaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_reaction_confirm_title)
            .setMessage(R.string.delete_reaction_confirm_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteReaction(reaction) }
            .show()
    }
}
