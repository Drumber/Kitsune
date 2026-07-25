package io.github.drumber.kitsune.ui.details.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMedia
import io.github.drumber.kitsune.data.presentation.dto.toMediaUnitDto
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.ui.showSnackbarOnFailure
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EpisodesFragment : Fragment(R.layout.fragment_media_list),
    NavigationBarView.OnItemReselectedListener {

    private val args: EpisodesFragmentArgs by navArgs()

    private val viewModel: EpisodesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        val items = viewModel.dataSource.collectAsLazyPagingItems()
        val libraryEntry by viewModel.libraryEntryWrapper.collectAsStateWithLifecycle()
        val title = getString(
            when (args.media.type) {
                MediaType.Anime -> R.string.title_episodes
                MediaType.Manga -> R.string.title_chapters
            }
        )
        EpisodesScreen(
            title = title,
            items = items,
            posterUrl = args.media.toMedia().posterImageUrl,
            isWatchCheckboxEnabled = libraryEntry != null,
            numberWatched = libraryEntry?.progress ?: 0,
            onNavigateUp = { findNavController().navigateUp() },
            onItemClick = { mediaUnit -> showDetailsBottomSheet(mediaUnit) },
            onWatchedChanged = { mediaUnit, isWatched ->
                viewModel.setMediaUnitWatched(mediaUnit, isWatched)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setMedia(args.media.toMedia())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.libraryUpdateResultFlow.collectLatest {
                    it.showSnackbarOnFailure(view)
                }
            }
        }
    }

    private fun showDetailsBottomSheet(mediaUnit: MediaUnit) {
        val sheetMediaUnit = MediaUnitDetailsBottomSheet()
        sheetMediaUnit.arguments = bundleOf(
            MediaUnitDetailsBottomSheet.BUNDLE_MEDIA_UNIT_ADAPTER to mediaUnit.toMediaUnitDto(),
            MediaUnitDetailsBottomSheet.BUNDLE_THUMBNAIL to args.media.toMedia().posterImageUrl
        )
        sheetMediaUnit.show(parentFragmentManager, MediaUnitDetailsBottomSheet.TAG)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        findNavController().navigateUp()
    }
}
