package io.github.drumber.kitsune.ui.library

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.core.searcher.Debouncer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult
import io.github.drumber.kitsune.ui.authentication.AuthenticationActivity
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibrarySynchronizationResult
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.ui.webview.WebViewFragmentDirections
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : BaseFragment(R.layout.fragment_library, true),
    NavigationBarView.OnItemReselectedListener {

    private val viewModel: LibraryViewModel by viewModel()

    private var lazyGridState: LazyGridState? = null

    private val autoSyncDebouncer by lazy { Debouncer(5000L) }

    companion object {
        const val RESULT_KEY_RATING = "library_rating_result_key"
        const val RESULT_KEY_REMOVE_RATING = "library_remove_rating_result_key"
        const val RESULT_KEY_EDIT_ENTRY_UPDATED = "library_edit_entry_updated"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { LibraryContent() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(RESULT_KEY_RATING) { _, bundle ->
            val rating = bundle.getInt(RatingBottomSheet.BUNDLE_RATING, -1)
            if (rating != -1) {
                viewModel.updateRating(rating)
            }
        }

        setFragmentResultListener(RESULT_KEY_REMOVE_RATING) { _, _ ->
            viewModel.updateRating(null)
        }

        setFragmentResultListener(RESULT_KEY_EDIT_ENTRY_UPDATED) { _, _ ->
            viewModel.triggerAdapterUpdate()
        }

        viewModel.notSynchronizedLibraryEntryModifications.observe(viewLifecycleOwner) { modifications ->
            if (!viewModel.hasUser()) return@observe

            viewModel.invalidatePagingSource()

            autoSyncDebouncer.debounce(viewLifecycleOwner.lifecycleScope) {
                val connectivityManager =
                    requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (modifications.isNotEmpty() && !connectivityManager.isActiveNetworkMetered) {
                    viewModel.synchronizeOfflineLibraryUpdates()
                }
            }
        }
    }

    @Composable
    private fun LibraryContent() {
        val uiState by viewModel.state.collectAsStateWithLifecycle()
        val libraryEntries = viewModel.pagingDataFlow.collectAsLazyPagingItems()
        val modifications by viewModel.notSynchronizedLibraryEntryModifications
            .collectAsStateWithLifecycle()
        val offlineSyncCount = modifications?.size ?: 0
        val localUser by viewModel.localUser.collectAsStateWithLifecycle()
        val gridState = rememberLazyGridState()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val errorUpdateFailed = stringResource(R.string.error_library_update_failed)
        val errorUpdateNotFound = stringResource(R.string.error_library_update_not_found)

        DisposableEffect(Unit) {
            lazyGridState = gridState
            onDispose { lazyGridState = null }
        }

        SideEffect {
            viewModel.doRefreshListener = { libraryEntries.refresh() }
        }

        LaunchedEffect(Unit) {
            viewModel.libraryChangeResultFlow.collect { result ->
                val msg = libraryChangeSnackbarMessage(
                    result, context, errorUpdateFailed, errorUpdateNotFound
                )
                if (msg != null) {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }

        LibraryScreen(
            uiState = uiState,
            libraryEntries = libraryEntries,
            offlineSyncCount = offlineSyncCount,
            isLoggedIn = localUser != null,
            gridState = gridState,
            snackbarHostState = snackbarHostState,
            onSearch = { viewModel.searchLibrary(it) },
            onKindSelected = { viewModel.setLibraryEntryKind(it) },
            onStatusToggle = { status ->
                val current = uiState.filter.libraryStatus.toMutableList()
                if (current.contains(status)) {
                    current.remove(status)
                } else {
                    current.add(status)
                }
                viewModel.setLibraryEntryStatus(current)
            },
            onSyncClicked = { viewModel.synchronizeOfflineLibraryUpdates() },
            onDbRequestClicked = { showDbRequestDialog() },
            onLoginClicked = {
                startActivity(Intent(requireActivity(), AuthenticationActivity::class.java))
            },
            onEntryClicked = { navigateToDetails(it) },
            onEntryLongClicked = { navigateToEditEntry(it) },
            onEpisodeWatched = { viewModel.markEpisodeWatched(it) },
            onEpisodeUnwatched = { viewModel.markEpisodeUnwatched(it) },
            onRatingClicked = { navigateToRating(it) }
        )
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        lifecycleScope.launch {
            lazyGridState?.animateScrollToItem(0)
        }
    }

    override fun onDestroyView() {
        viewModel.doRefreshListener = null
        lazyGridState = null
        super.onDestroyView()
    }

    private fun navigateToDetails(item: LibraryEntryWithModification) {
        val media = item.libraryEntry.media ?: return
        val action = LibraryFragmentDirections.actionLibraryFragmentToDetailsFragment(media.toMediaDto())
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    private fun navigateToEditEntry(item: LibraryEntryWithModification) {
        val action = LibraryFragmentDirections.actionLibraryFragmentToLibraryEditEntryFragment(
            item.libraryEntry.id,
            RESULT_KEY_EDIT_ENTRY_UPDATED
        )
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    private fun navigateToRating(item: LibraryEntryWithModification) {
        viewModel.lastRatedLibraryEntry = item.libraryEntry

        val action = LibraryFragmentDirections.actionLibraryFragmentToRatingBottomSheet(
            title = item.media?.title ?: "",
            ratingTwenty = item.ratingTwenty ?: -1,
            ratingResultKey = RESULT_KEY_RATING,
            removeResultKey = RESULT_KEY_REMOVE_RATING,
            ratingSystem = RatingSystemUtil.getRatingSystem()
        )
        findNavController().navigateSafe(R.id.library_fragment, action)
    }

    private fun showDbRequestDialog() {
        val options = listOf(
            R.string.db_request_anime to Kitsu.ANIME_DB_REQUEST_URL,
            R.string.db_request_open_anime to Kitsu.OPEN_ANIME_REQUESTS_URL,
            R.string.db_request_manga to Kitsu.MANGA_DB_REQUEST_URL,
            R.string.db_request_open_manga to Kitsu.OPEN_MANGA_REQUESTS_URL
        )
        val items = options.map { getString(it.first) }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_db_request)
            .setItems(items) { _, which ->
                val url = options[which].second
                val action = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
                findNavController().navigateSafe(R.id.library_fragment, action)
            }
            .show()
    }
}

private fun libraryChangeSnackbarMessage(
    result: LibraryChangeResult,
    context: Context,
    errorUpdateFailed: String,
    errorUpdateNotFound: String): String? = when (result) {
    is LibraryUpdateResult -> when (val r = result.result) {
        is LibraryEntryUpdateResult.Success -> null
        is LibraryEntryUpdateResult.Failure -> when (r.reason) {
            is LibraryEntryUpdateFailureReason.NotFound -> errorUpdateNotFound
            else -> errorUpdateFailed
        }
    }
    is LibrarySynchronizationResult -> {
        val failedCount = result.results.count { it !is LibraryEntryUpdateResult.Success }
        when {
            failedCount == 0 -> null
            failedCount == 1 -> errorUpdateFailed
            else -> context.getString(R.string.error_library_update_failed_multiple, failedCount)
        }
    }
}
