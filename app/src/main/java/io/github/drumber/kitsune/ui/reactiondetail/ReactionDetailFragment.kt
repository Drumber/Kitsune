package io.github.drumber.kitsune.ui.reactiondetail

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.databinding.FragmentReactionDetailBinding
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.scrollView.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        binding.btnUpvote.setOnClickListener { viewModel.upvote() }

        val glide = Glide.with(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { loading ->
                    binding.progressBar.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reaction.collectLatest { reaction ->
                    reaction?.let { bindReaction(it, glide) }
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
                viewModel.upvoteEvents.collectLatest { event ->
                    when (event) {
                        is ReactionDetailViewModel.UpvoteEvent.Success ->
                            binding.btnUpvote.text = event.newCount.toString()

                        ReactionDetailViewModel.UpvoteEvent.LoginRequired ->
                            showSnackbar(R.string.reactions_upvote_login_required)

                        ReactionDetailViewModel.UpvoteEvent.Failed ->
                            showSnackbar(R.string.reactions_upvote_failed)
                    }
                }
            }
        }
    }

    private fun bindReaction(reaction: MediaReaction, glide: com.bumptech.glide.RequestManager) {
        glide.load(reaction.authorAvatarUrl)
            .placeholder(R.drawable.ic_outline_person_24)
            .circleCrop()
            .into(binding.ivAvatar)

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
            glide.load(reaction.mediaPosterUrl)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivMediaPoster)
            binding.cardMedia.setOnClickListener { openMedia(reaction) }
        }

        binding.btnUpvote.text = reaction.upVotesCount.toString()
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
}
