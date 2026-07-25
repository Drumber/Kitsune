package io.github.drumber.kitsune.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMedia
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.databinding.DialogComposeReactionBinding
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.AddNewLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.DeleteLibraryEntryFailed
import io.github.drumber.kitsune.ui.details.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.extensions.startUrlShareIntent
import io.github.drumber.kitsune.util.logW
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.showSnackbar
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle as collectFlowAsStateWithLifecycle

class DetailsFragment : BaseFragment(R.layout.fragment_details, true),
    NavigationBarView.OnItemReselectedListener {

    private val args: DetailsFragmentArgs by navArgs()

    private val viewModel: DetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val media by viewModel.mediaModel.collectAsStateWithLifecycle()
        val libraryEntry by viewModel.libraryEntryWrapper.collectAsStateWithLifecycle()
        val favorite by viewModel.favorite.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val reactions by viewModel.reactions.collectFlowAsStateWithLifecycle(emptyList())
        DetailsScreen(
            media = media,
            libraryEntry = libraryEntry,
            favorite = favorite,
            reactions = reactions,
            isLoading = isLoading == true,
            isLoggedIn = viewModel.isLoggedIn(),
            onNavigateUp = { findNavController().navigateUp() },
            onShareMedia = { shareMedia() },
            onToggleFavorite = { toggleFavorite() },
            onOpenExternal = {
                viewModel.loadMappingsIfNotAlreadyLoaded()
                MediaMappingsBottomSheet().show(childFragmentManager, MediaMappingsBottomSheet.TAG)
            },
            onManageLibrary = { showManageLibraryBottomSheet() },
            onEditLibraryEntry = { showEditLibraryEntryFragment() },
            onNavigateToEpisodes = {
                media?.let { m ->
                    findNavController().navigate(
                        DetailsFragmentDirections.actionDetailsFragmentToEpisodesFragment(m.toMediaDto())
                    )
                }
            },
            onNavigateToCharacters = {
                media?.let { m ->
                    findNavController().navigate(
                        DetailsFragmentDirections.actionDetailsFragmentToCharactersFragment(
                            m.id, m is Anime
                        )
                    )
                }
            },
            onNavigateToFeed = {
                media?.let { m ->
                    findNavController().navigate(
                        DetailsFragmentDirections.actionDetailsFragmentToMediaFeedFragment(
                            m.id, m is Anime
                        )
                    )
                }
            },
            onNavigateToReactions = {
                media?.let { m ->
                    findNavController().navigate(
                        DetailsFragmentDirections.actionDetailsFragmentToReactionsFragment(
                            m.id, m is Anime
                        )
                    )
                }
            },
            onNavigateToCategory = { category -> navigateToCategory(category, media) },
            onNavigateToFranchise = { franchiseMedia ->
                findNavController().navigateSafe(
                    R.id.details_fragment,
                    DetailsFragmentDirections.actionDetailsFragmentSelf(franchiseMedia.toMediaDto())
                )
            },
            onUpvoteReaction = { viewModel.upvoteReaction(it) },
            onAddReaction = { showComposeReactionDialog() },
            onCoverClick = {},
            onPosterClick = {},
            onOpenStreamingLink = { url ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMediaFromArgs()
        setupLibraryResultListeners()
        collectLibraryChangeResults(view)
    }

    private fun initMediaFromArgs() {
        if (args.media != null) {
            viewModel.initMediaModel(args.media!!.toMedia())
        } else if (!args.type.isNullOrBlank() && !args.slug.isNullOrBlank()) {
            val isAnime = when (args.type!!.lowercase()) {
                "anime" -> true
                "manga" -> false
                else -> null
            }
            if (isAnime == null) {
                logW("Unknown media type '${args.type}'.")
                showSomethingWrongToast()
                findNavController().navigateUp()
            } else {
                viewModel.initFromDeepLink(isAnime, args.slug!!)
            }
        } else {
            logW("DetailsFragment opened without media bundle or invalid deeplink parameters.")
            showSomethingWrongToast()
            findNavController().navigateUp()
        }
    }

    private fun setupLibraryResultListeners() {
        setFragmentResultListener(ManageLibraryBottomSheet.STATUS_REQUEST_KEY) { _, bundle ->
            val status = BundleCompat.getSerializable(
                bundle, ManageLibraryBottomSheet.BUNDLE_STATUS, LibraryStatus::class.java
            )
            status?.let { viewModel.updateLibraryEntryStatus(it) }
        }
        setFragmentResultListener(ManageLibraryBottomSheet.REMOVE_REQUEST_KEY) { _, bundle ->
            val shouldRemove = !bundle.getBoolean(ManageLibraryBottomSheet.BUNDLE_EXISTS_IN_LIBRARY)
            if (shouldRemove) {
                viewModel.removeLibraryEntry()
            }
        }
    }

    private fun collectLibraryChangeResults(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.libraryChangeResultFlow.collectLatest { result ->
                when (result) {
                    is LibraryUpdateResult -> result.result.showSnackbarOnFailure(view)
                    is AddNewLibraryEntryFailed -> showSnackbar(view, R.string.error_library_add_failed)
                    is DeleteLibraryEntryFailed -> showSnackbar(view, R.string.error_library_delete_failed)
                }
            }
        }
    }

    private fun shareMedia() {
        val url = viewModel.mediaModel.value?.let { m ->
            val prefix = if (m is Anime) Kitsu.ANIME_URL_PREFIX else Kitsu.MANGA_URL_PREFIX
            prefix + m.slug
        }
        if (url != null) {
            startUrlShareIntent(url)
        } else {
            showSomethingWrongToast()
        }
    }

    private fun toggleFavorite() {
        if (viewModel.isLoggedIn()) {
            viewModel.toggleFavorite()
        } else {
            showLogInSnackbar()
        }
    }

    private fun navigateToCategory(category: Category, media: Media?) {
        val categorySlug = category.slug ?: return
        val title = category.title ?: getString(R.string.no_information)
        val mediaSelector = MediaSelector(
            if (media is Anime) MediaType.Anime else MediaType.Manga,
            Filter().filter("categories", categorySlug).sort(SortFilter.POPULARITY_DESC.queryParam).options
        )
        findNavController().navigate(
            DetailsFragmentDirections.actionDetailsFragmentToMediaListFragment(mediaSelector, title)
        )
    }

    private fun showManageLibraryBottomSheet() {
        if (viewModel.isLoggedIn()) {
            viewModel.mediaModel.value?.let { mediaModel ->
                val sheetManageLibrary = ManageLibraryBottomSheet()
                sheetManageLibrary.arguments = bundleOf(
                    ManageLibraryBottomSheet.BUNDLE_TITLE to mediaModel.title,
                    ManageLibraryBottomSheet.BUNDLE_IS_ANIME to (mediaModel is Anime),
                    ManageLibraryBottomSheet.BUNDLE_EXISTS_IN_LIBRARY to
                        (viewModel.libraryEntryWrapper.value != null)
                )
                sheetManageLibrary.show(parentFragmentManager, ManageLibraryBottomSheet.TAG)
            }
        } else {
            showLogInSnackbar()
        }
    }

    private fun showEditLibraryEntryFragment() {
        if (!viewModel.isLoggedIn()) return
        val entryId = viewModel.libraryEntryWrapper.value?.libraryEntry?.id ?: return
        findNavController().navigateSafe(
            R.id.details_fragment,
            DetailsFragmentDirections.actionDetailsFragmentToLibraryEditEntryFragment(entryId)
        )
    }

    private fun showComposeReactionDialog() {
        val dialogBinding = DialogComposeReactionBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reaction_compose_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.reaction_compose_action_post) { _, _ ->
                val text = dialogBinding.etReaction.text?.toString().orEmpty().trim()
                if (text.isNotEmpty()) viewModel.createReaction(text)
            }
            .show()
    }

    private fun showLogInSnackbar() {
        Snackbar.make(requireView(), R.string.info_log_in_required, Snackbar.LENGTH_LONG).apply {
            view.initMarginWindowInsetsListener(left = true, right = true, bottom = true)
            setAction(R.string.action_log_in) {
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }.show()
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }
}
