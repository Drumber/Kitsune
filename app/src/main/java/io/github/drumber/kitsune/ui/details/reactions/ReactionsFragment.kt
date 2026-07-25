package io.github.drumber.kitsune.ui.details.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.databinding.DialogComposeReactionBinding
import io.github.drumber.kitsune.ui.compose.composeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReactionsFragment : Fragment(R.layout.fragment_reactions),
    NavigationBarView.OnItemReselectedListener {

    private val args: ReactionsFragmentArgs by navArgs()

    private val viewModel: ReactionsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val items = viewModel.dataSource.collectAsLazyPagingItems()
        ReactionsScreen(
            title = getString(R.string.title_reactions),
            items = items,
            currentUserId = viewModel.currentUserId,
            onNavigateUp = { findNavController().navigateUp() },
            onAddReactionClick = { showComposeReactionDialog(null) },
            onUpvoteClick = { viewModel.upvote(it) },
            onEditClick = { reaction -> showComposeReactionDialog(reaction) },
            onDeleteClick = { reaction -> confirmDeleteReaction(reaction) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.mediaId, args.isAnime)
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

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }
}
