package io.github.drumber.kitsune.ui.reactiondetail

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
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.reactiondetail.compose.ReactionDetailScreen
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReactionDetailFragment : Fragment() {

    private val args: ReactionDetailFragmentArgs by navArgs()

    private val viewModel: ReactionDetailViewModel by viewModel {
        parametersOf(args.reactionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { Content() }

    @Composable
    private fun Content() {
        val reaction by viewModel.reaction.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val isUpvoted by viewModel.isUpvoted.collectAsStateWithLifecycle()
        var snackbarMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            viewModel.upvoteEvents.collect { event ->
                snackbarMessage = when (event) {
                    is ReactionDetailViewModel.UpvoteEvent.Success -> null
                    ReactionDetailViewModel.UpvoteEvent.LoginRequired ->
                        getString(R.string.reactions_upvote_login_required)
                    ReactionDetailViewModel.UpvoteEvent.Failed ->
                        getString(R.string.reactions_upvote_failed)
                }
            }
        }

        ReactionDetailScreen(
            reaction = reaction,
            isLoading = isLoading,
            isUpvoted = isUpvoted,
            snackbarMessage = snackbarMessage,
            onSnackbarShown = { snackbarMessage = null },
            onNavigateUp = { findNavController().navigateUp() },
            onUpvote = { viewModel.upvote() },
            onMediaClick = { navigateToMedia() }
        )
    }

    private fun navigateToMedia() {
        val reaction = viewModel.reaction.value ?: return
        val slug = reaction.mediaSlug
        val isAnime = reaction.mediaIsAnime
        if (slug.isNullOrBlank() || isAnime == null) return
        val action = DetailsFragmentDirections.actionGlobalDetailsFragment(
            type = if (isAnime) "anime" else "manga",
            slug = slug
        )
        findNavController().navigateSafe(R.id.reaction_detail_fragment, action)
    }
}
