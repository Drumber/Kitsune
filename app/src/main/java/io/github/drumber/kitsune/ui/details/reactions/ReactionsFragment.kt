package io.github.drumber.kitsune.ui.details.reactions

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.databinding.DialogComposeReactionBinding
import io.github.drumber.kitsune.databinding.FragmentReactionsBinding
import io.github.drumber.kitsune.di.SocialImagesLoader
import io.github.drumber.kitsune.ui.adapter.paging.MediaReactionPagingAdapter
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.component.updateLoadState
import io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
import io.github.drumber.kitsune.ui.reactiondetail.ReactionDetailFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.setAppTheme
import io.github.drumber.kitsune.util.extensions.smoothScrollOrJumpToTop
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import io.github.drumber.kitsune.util.ui.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class ReactionsFragment : Fragment(R.layout.fragment_reactions),
    NavigationBarView.OnItemReselectedListener {

    private val args: ReactionsFragmentArgs by navArgs()

    private val binding by viewBinding(FragmentReactionsBinding::bind)

    private val viewModel: ReactionsViewModel by viewModel()

    private val imageLoader: ImageLoader by inject(named<SocialImagesLoader>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.mediaId, args.isAnime)

        binding.apply {
            collapsingToolbar.initWindowInsetsListener(consume = false)
            toolbar.initWindowInsetsListener(false)
            toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
            rvReactions.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
            fabAddReaction.initMarginWindowInsetsListener(
                right = true,
                bottom = true,
                consume = false
            )
            fabAddReaction.setOnClickListener { showComposeReactionDialog(null) }
        }

        val adapter = MediaReactionPagingAdapter(
            imageLoader = imageLoader,
            currentUserId = viewModel.currentUserId,
            onItemClick = { reaction -> navigateToReaction(reaction) },
            onUpvoteClick = { reaction -> viewModel.upvote(reaction) },
            onEditClick = { reaction -> showComposeReactionDialog(reaction) },
            onDeleteClick = { reaction -> confirmDeleteReaction(reaction) },
            onShareClick = { reaction -> showShareMenu(reaction) },
            onAuthorClick = { userId -> navigateToUserProfile(userId) },
        )
        binding.rvReactions.adapter = adapter.withLoadStateFooter(
            footer = ResourceLoadStateAdapter(adapter)
        )
        binding.rvReactions.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        binding.layoutLoading.btnRetry.setOnClickListener { adapter.retry() }

        binding.swipeRefreshLayout.apply {
            setAppTheme()
            setOnRefreshListener { adapter.refresh() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { loadState ->
                    binding.layoutLoading.updateLoadState(
                        binding.rvReactions,
                        adapter.itemCount,
                        loadState
                    )
                    binding.swipeRefreshLayout.isRefreshing =
                        loadState.refresh is LoadState.Loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataSource.collectLatest { data ->
                    adapter.submitData(data)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.upvoteEvents.collectLatest { event ->
                    when (event) {
                        is ReactionsViewModel.UpvoteEvent.Success ->
                            adapter.markUpvoted(event.reactionId, event.newCount)

                        ReactionsViewModel.UpvoteEvent.LoginRequired ->
                            showSnackbar(R.string.reactions_upvote_login_required)

                        ReactionsViewModel.UpvoteEvent.Failed ->
                            showSnackbar(R.string.reactions_upvote_failed)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.editEvents.collectLatest { event ->
                    when (event) {
                        ReactionsViewModel.EditEvent.LoginRequired ->
                            showSnackbar(R.string.reaction_login_required)

                        ReactionsViewModel.EditEvent.AddToLibraryRequired ->
                            showSnackbar(R.string.reaction_add_to_library_required)

                        ReactionsViewModel.EditEvent.Created -> {
                            showSnackbar(R.string.reaction_posted)
                            adapter.refresh()
                        }

                        ReactionsViewModel.EditEvent.Updated -> {
                            showSnackbar(R.string.reaction_updated)
                            adapter.refresh()
                        }

                        ReactionsViewModel.EditEvent.Deleted -> {
                            showSnackbar(R.string.reaction_deleted)
                            adapter.refresh()
                        }

                        ReactionsViewModel.EditEvent.Failed ->
                            showSnackbar(R.string.action_failed)
                    }
                }
            }
        }
    }

    private fun showComposeReactionDialog(existing: MediaReaction?) {
        val dialogBinding = DialogComposeReactionBinding.inflate(layoutInflater)
        val initialText = existing?.reaction?.takeUnless { it.isBlank() } ?: existing?.content
        dialogBinding.etReaction.setText(initialText)
        dialogBinding.etReaction.setSelection(dialogBinding.etReaction.text?.length ?: 0)

        val titleRes = if (existing == null) {
            R.string.reaction_compose_title
        } else {
            R.string.reaction_compose_edit_title
        }
        val positiveRes = if (existing == null) {
            R.string.reaction_compose_action_post
        } else {
            R.string.action_save
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(positiveRes) { _, _ ->
                val text = dialogBinding.etReaction.text?.toString().orEmpty().trim()
                if (text.isEmpty()) return@setPositiveButton
                if (existing == null) {
                    viewModel.createReaction(text)
                } else {
                    viewModel.updateReaction(existing, text)
                }
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

    private fun showShareMenu(reaction: MediaReaction) {
        startUrlShareIntent("${Kitsu.BASE_URL}/media-reactions/${reaction.id}")
    }

    private fun showSnackbar(messageResId: Int) {
        Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToReaction(reaction: MediaReaction) {
        val action = ReactionDetailFragmentDirections
            .actionGlobalReactionDetailFragment(reaction.id)
        findNavController().navigateSafe(R.id.reactions_fragment, action)
    }

    private fun navigateToUserProfile(userId: String) {
        val action = UserProfileFragmentDirections.actionGlobalUserProfileFragment(userId)
        findNavController().navigateSafe(R.id.reactions_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (binding.rvReactions.canScrollVertically(-1)) {
            binding.rvReactions.smoothScrollOrJumpToTop()
            binding.appBarLayout.setExpanded(true)
        } else {
            findNavController().navigateUp()
        }
    }
}
