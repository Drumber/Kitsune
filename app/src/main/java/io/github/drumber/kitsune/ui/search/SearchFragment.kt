package io.github.drumber.kitsune.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.model.response.ResponseSearch
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.algolia.SearchType
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.user.UserSearchResult
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.main.FragmentDecorationPreference
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.lang.ref.WeakReference

class SearchFragment : Fragment(),
    FragmentDecorationPreference,
    NavigationBarView.OnItemReselectedListener {

    override val hasTransparentStatusBar = false

    private val viewModel: SearchViewModel by activityViewModel()

    private val args: SearchFragmentArgs by navArgs()

    private val connectionHandler = ConnectionHandler()

    private val searchBoxView = SearchBoxViewCompose()

    private var lazyGridState: LazyGridState? = null
    private var lazyListState: LazyListState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        SearchContent()
    }

    @Composable
    private fun SearchContent() {
        val query by searchBoxView.queryFlow.collectAsState()
        val searchType by viewModel.currentSearchType.collectAsStateWithLifecycle(SearchType.Media)
        val clientStatus by viewModel.searchClientStatus.collectAsStateWithLifecycle()
        val filters by viewModel.filtersLiveData.collectAsStateWithLifecycle()
        val filterCount = filters?.getFilters()?.size ?: 0
        val searchItems = viewModel.searchResultSource.collectAsLazyPagingItems()

        val gridState = rememberLazyGridState()
        val listState = rememberLazyListState()
        DisposableEffect(Unit) {
            lazyGridState = gridState
            lazyListState = listState
            onDispose {
                lazyGridState = null
                lazyListState = null
            }
        }

        SearchScreen(
            query = query,
            isSearchFocused = args.focusSearch,
            onQueryChange = { searchBoxView.notifyQueryChanged(it) },
            onSearchFocusChange = {},
            searchType = searchType,
            onSearchTypeChange = { viewModel.switchSearchType(it) },
            clientStatus = clientStatus ?: SearchViewModel.SearchClientStatus.NotInitialized,
            onRetrySearchClient = { viewModel.initializeSearchClient() },
            filterCount = filterCount,
            onFilterClick = { navigateToFacet() },
            onFilterLongClick = {
                if (!viewModel.filtersLiveData.value?.getFilters().isNullOrEmpty()) {
                    viewModel.clearSearchFilter()
                }
            },
            gridState = gridState,
            columnState = listState,
            searchItems = searchItems,
            onMediaClick = { media -> navigateToDetails(media) },
            onUserClick = { user -> navigateToUserProfile(user) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSearchBox()
    }

    private fun observeSearchBox() {
        viewModel.searchBox.observe(viewLifecycleOwner) { searchBox ->
            connectionHandler.clear()
            connectionHandler += searchBox.connectView(searchBoxView)
            connectionHandler += SearchResponseListener(searchBox) {
                lazyGridState?.requestScrollToItem(0)
                lazyListState?.requestScrollToItem(0)
            }
        }
    }

    private fun navigateToDetails(media: Media) {
        val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(media.toMediaDto())
        findNavController().navigateSafe(R.id.search_fragment, action)
    }

    private fun navigateToFacet() {
        val action = SearchFragmentDirections.actionSearchFragmentToFacetFragment()
        findNavController().navigateSafe(R.id.search_fragment, action)
    }

    private fun navigateToUserProfile(user: UserSearchResult) {
        val action = io.github.drumber.kitsune.ui.profile.UserProfileFragmentDirections
            .actionGlobalUserProfileFragment(user.id, user.name)
        findNavController().navigateSafe(R.id.search_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val gridState = lazyGridState
        val listState = lazyListState
        if (gridState != null &&
            (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0)) {
            gridState.requestScrollToItem(0)
        } else if (listState != null &&
            (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)) {
            listState.requestScrollToItem(0)
        }
    }

    override fun onDestroyView() {
        connectionHandler.clear()
        super.onDestroyView()
    }

    private class SearchResponseListener(
        searchBox: SearchBoxConnector<ResponseSearch>,
        private val onSearchReceived: () -> Unit
    ) : AbstractConnection() {

        private val _searchBox = WeakReference(searchBox)
        private var pendingSearch = false

        private val onQueryChanged = { _: Any? -> pendingSearch = true }
        private val onSearchResponse = { r: ResponseSearch? ->
            if (pendingSearch) onSearchReceived()
            if (pendingSearch && r?.pageOrNull == 0) pendingSearch = false
        }

        override fun connect() {
            super.connect()
            _searchBox.get()?.let {
                it.viewModel.query.subscribe(onQueryChanged)
                it.searcher.response.subscribe(onSearchResponse)
            }
        }

        override fun disconnect() {
            super.disconnect()
            _searchBox.get()?.let {
                it.viewModel.query.unsubscribe(onQueryChanged)
                it.searcher.response.unsubscribe(onSearchResponse)
            }
        }
    }
}
