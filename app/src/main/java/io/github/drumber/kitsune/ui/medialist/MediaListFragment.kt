package io.github.drumber.kitsune.ui.medialist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.dimensionResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.extensions.navigateSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MediaListFragment : Fragment(), NavigationBarView.OnItemReselectedListener {

    private val args: MediaListFragmentArgs by navArgs()

    /**
     * Holds the [LazyGridState] created inside [MediaListContent] so that
     * [onNavigationItemReselected] can animate a scroll-to-top without going through Compose state.
     */
    private var lazyGridState: LazyGridState? = null

    /** Held alongside [lazyGridState] so reselect can re-expand the collapsed toolbar. */
    private var topAppBarState: TopAppBarState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        MediaListContent()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("UNCHECKED_CAST")
    @Composable
    private fun MediaListContent() {
        val viewModel: MediaListViewModel = koinViewModel()

        LaunchedEffect(args.mediaSelector) {
            viewModel.setMediaSelector(args.mediaSelector)
        }

        val items = (viewModel.dataSource as Flow<PagingData<Media>>).collectAsLazyPagingItems()

        val gridState = rememberLazyGridState()
        val appBarState = rememberTopAppBarState()
        DisposableEffect(Unit) {
            lazyGridState = gridState
            topAppBarState = appBarState
            onDispose {
                lazyGridState = null
                topAppBarState = null
            }
        }

        val itemWidth = dimensionResource(KitsunePref.mediaItemSize.widthRes)
        val itemHeight = dimensionResource(KitsunePref.mediaItemSize.heightRes)
        val itemMargin = dimensionResource(R.dimen.media_item_margin)
        val columns = GridCells.Adaptive(itemWidth + itemMargin * 2)
        val itemAspectRatio = itemWidth / itemHeight

        MediaListScreen(
            title = args.title,
            items = items,
            columns = columns,
            itemAspectRatio = itemAspectRatio,
            gridState = gridState,
            topAppBarState = appBarState,
            onNavigateUp = { findNavController().navigateUp() },
            onMediaClick = { media -> navigateToDetails(media) }
        )
    }

    private fun navigateToDetails(media: Media) {
        val action = MediaListFragmentDirections
            .actionMediaListFragmentToDetailsFragment(media.toMediaDto())
        findNavController().navigateSafe(R.id.media_list_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val state = lazyGridState
        if (state != null &&
            (state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0)
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                state.animateScrollToItem(0)
                // mirrors the old appBarLayout.setExpanded(true)
                topAppBarState?.apply {
                    heightOffset = 0f
                    contentOffset = 0f
                }
            }
        } else {
            findNavController().navigateUp()
        }
    }
}