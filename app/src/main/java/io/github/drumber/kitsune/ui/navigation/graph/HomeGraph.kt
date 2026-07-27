package io.github.drumber.kitsune.ui.navigation.graph

import android.content.Intent
import android.net.ConnectivityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.filter.facet.connectView
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.library.LibraryScreen
import io.github.drumber.kitsune.ui.library.LibraryViewModel
import io.github.drumber.kitsune.ui.library.RatingScreen
import io.github.drumber.kitsune.ui.library.editentry.LibraryEditEntryScreen
import io.github.drumber.kitsune.ui.library.editentry.LibraryEditEntryViewModel
import io.github.drumber.kitsune.ui.main.HomeExploreSectionUiState
import io.github.drumber.kitsune.ui.main.HomeExploreScreen
import io.github.drumber.kitsune.ui.main.MainFragmentViewModel
import io.github.drumber.kitsune.ui.main.MainScreen
import io.github.drumber.kitsune.ui.medialist.MediaListScreen
import io.github.drumber.kitsune.ui.medialist.MediaListViewModel
import io.github.drumber.kitsune.ui.navigation.NavResultEffect
import io.github.drumber.kitsune.ui.navigation.NavResults
import io.github.drumber.kitsune.ui.navigation.LocalReselectEvents
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.navigation.navigateSafe
import io.github.drumber.kitsune.ui.navigation.setNavResult
import io.github.drumber.kitsune.ui.navigation.toMediaListRoute
import io.github.drumber.kitsune.ui.navigation.toMediaSelector
import io.github.drumber.kitsune.ui.search.SearchBoxViewCompose
import io.github.drumber.kitsune.ui.search.SearchScreen
import io.github.drumber.kitsune.ui.search.SearchViewModel
import io.github.drumber.kitsune.ui.search.categories.CategoriesScreen
import io.github.drumber.kitsune.ui.search.categories.CategoriesViewModel
import io.github.drumber.kitsune.ui.search.categories.rememberCategoryRows
import io.github.drumber.kitsune.ui.search.filter.FacetListViewState
import io.github.drumber.kitsune.ui.search.filter.FacetScreen
import io.github.drumber.kitsune.ui.search.filter.NumberRangeViewState
import io.github.drumber.kitsune.ui.component.algolia.range.connectView
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.network.ResponseData
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.rating.RatingSystemUtil
import io.github.drumber.kitsune.util.stripTimeUtcMillis
import io.github.drumber.kitsune.util.toDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.androidx.compose.koinViewModel
import java.lang.ref.WeakReference
import java.util.Calendar
import java.util.TimeZone

fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    composable<Routes.Home> {
        HomeDestination(navController)
    }

    composable<Routes.Search>(
        deepLinks = listOf(navDeepLink { uriPattern = "kitsune://search" })
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.Search>()
        SearchDestination(navController, route.focusSearch)
    }

    composable<Routes.Facet> {
        FacetDestination(navController)
    }

    composable<Routes.Categories> {
        CategoriesDestination(navController)
    }

    composable<Routes.MediaList> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.MediaList>()
        MediaListDestination(navController, route)
    }

    composable<Routes.Library>(
        deepLinks = listOf(navDeepLink { uriPattern = "kitsune://library" })
    ) { backStackEntry ->
        LibraryDestination(navController, backStackEntry)
    }

    composable<Routes.LibraryEditEntry> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.LibraryEditEntry>()
        LibraryEditEntryDestination(navController, route.libraryEntryId)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeDestination(navController: NavHostController) {
    val viewModel: MainFragmentViewModel = koinViewModel()

    val pagerState = rememberPagerState { 2 }
    val animeScrollState = rememberScrollState()
    val mangaScrollState = rememberScrollState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Stub: scroll-to-top from nav-bar reselect is handled by the shell in a later phase.
    val scrollToTopEvents = LocalReselectEvents.current

    LaunchedEffect(Unit) {
        viewModel.reloadFinished.collect { finished ->
            if (finished) isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        scrollToTopEvents.collect {
            when (pagerState.currentPage) {
                0 -> animeScrollState.animateScrollTo(0)
                1 -> mangaScrollState.animateScrollTo(0)
            }
        }
    }

    MainScreen(
        animeExploreScreenContent = {
            AnimeExploreContent(viewModel = viewModel, navController = navController)
        },
        mangaExploreScreenContent = {
            MangaExploreContent(viewModel = viewModel, navController = navController)
        },
        isRefreshing = isRefreshing,
        onRefresh = { isAnime ->
            isRefreshing = true
            if (isAnime) viewModel.refreshAnimeData() else viewModel.refreshMangaData()
        },
        onSearchClick = { navController.navigateSafe(Routes.Search(focusSearch = true)) },
        pagerState = pagerState,
        animeScrollState = animeScrollState,
        mangaScrollState = mangaScrollState
    )
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun AnimeExploreContent(viewModel: MainFragmentViewModel, navController: NavHostController) {
    val trendingTitle = stringResource(R.string.section_trending)
    val topAiringTitle = stringResource(R.string.section_top_airing_anime)
    val topUpcomingTitle = stringResource(R.string.section_top_upcoming_anime)
    val highestRatedTitle = stringResource(R.string.section_highest_rated_anime)
    val mostPopularTitle = stringResource(R.string.section_most_popular_anime)

    val trendingState by (viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TRENDING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val topAiringState by (viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TOP_AIRING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val topUpcomingState by (viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TOP_UPCOMING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val highestRatedState by (viewModel.getAnimeExploreLiveData(MainFragmentViewModel.HIGHEST_RATED)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val mostPopularState by (viewModel.getAnimeExploreLiveData(MainFragmentViewModel.MOST_POPULAR)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()

    val topAiringSelector = MediaSelector(MediaType.Anime, MainFragmentViewModel.FILTER_TOP_AIRING_ANIME.options, RequestType.ALL)
    val topUpcomingSelector = MediaSelector(MediaType.Anime, MainFragmentViewModel.FILTER_TOP_UPCOMING_ANIME.options, RequestType.ALL)
    val highestRatedSelector = MediaSelector(MediaType.Anime, MainFragmentViewModel.FILTER_HIGHEST_RATED_ANIME.options, RequestType.ALL)
    val mostPopularSelector = MediaSelector(MediaType.Anime, MainFragmentViewModel.FILTER_MOST_POPULAR_ANIME.options, RequestType.ALL)

    HomeExploreScreen(
        sections = listOf(
            HomeExploreSectionUiState(trendingTitle, trendingState) {
                navController.navigateSafe(
                    MediaSelector(MediaType.Anime, Filter().limit(30).options, RequestType.TRENDING)
                        .toMediaListRoute(trendingTitle)
                )
            },
            HomeExploreSectionUiState(topAiringTitle, topAiringState) {
                navController.navigateSafe(topAiringSelector.toMediaListRoute(topAiringTitle))
            },
            HomeExploreSectionUiState(topUpcomingTitle, topUpcomingState) {
                navController.navigateSafe(topUpcomingSelector.toMediaListRoute(topUpcomingTitle))
            },
            HomeExploreSectionUiState(highestRatedTitle, highestRatedState) {
                navController.navigateSafe(highestRatedSelector.toMediaListRoute(highestRatedTitle))
            },
            HomeExploreSectionUiState(mostPopularTitle, mostPopularState) {
                navController.navigateSafe(mostPopularSelector.toMediaListRoute(mostPopularTitle))
            }
        ),
        onItemClick = { media ->
            navController.navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
        },
        onRetry = viewModel::refreshAnimeData
    )
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun MangaExploreContent(viewModel: MainFragmentViewModel, navController: NavHostController) {
    val trendingTitle = stringResource(R.string.section_trending)
    val topAiringTitle = stringResource(R.string.section_top_airing_manga)
    val topUpcomingTitle = stringResource(R.string.section_top_upcoming_manga)
    val highestRatedTitle = stringResource(R.string.section_highest_rated_manga)
    val mostPopularTitle = stringResource(R.string.section_most_popular_manga)

    val trendingState by (viewModel.getMangaExploreLiveData(MainFragmentViewModel.TRENDING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val topAiringState by (viewModel.getMangaExploreLiveData(MainFragmentViewModel.TOP_AIRING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val topUpcomingState by (viewModel.getMangaExploreLiveData(MainFragmentViewModel.TOP_UPCOMING)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val highestRatedState by (viewModel.getMangaExploreLiveData(MainFragmentViewModel.HIGHEST_RATED)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()
    val mostPopularState by (viewModel.getMangaExploreLiveData(MainFragmentViewModel.MOST_POPULAR)
            as androidx.lifecycle.LiveData<ResponseData<List<Media>>>).collectAsStateWithLifecycle()

    val topAiringSelector = MediaSelector(MediaType.Manga, MainFragmentViewModel.FILTER_TOP_AIRING_MANGA.options, RequestType.ALL)
    val topUpcomingSelector = MediaSelector(MediaType.Manga, MainFragmentViewModel.FILTER_TOP_UPCOMING_MANGA.options, RequestType.ALL)
    val highestRatedSelector = MediaSelector(MediaType.Manga, MainFragmentViewModel.FILTER_HIGHEST_RATED_MANGA.options, RequestType.ALL)
    val mostPopularSelector = MediaSelector(MediaType.Manga, MainFragmentViewModel.FILTER_MOST_POPULAR_MANGA.options, RequestType.ALL)

    HomeExploreScreen(
        sections = listOf(
            HomeExploreSectionUiState(trendingTitle, trendingState) {
                navController.navigateSafe(
                    MediaSelector(MediaType.Manga, Filter().limit(30).options, RequestType.TRENDING)
                        .toMediaListRoute(trendingTitle)
                )
            },
            HomeExploreSectionUiState(topAiringTitle, topAiringState) {
                navController.navigateSafe(topAiringSelector.toMediaListRoute(topAiringTitle))
            },
            HomeExploreSectionUiState(topUpcomingTitle, topUpcomingState) {
                navController.navigateSafe(topUpcomingSelector.toMediaListRoute(topUpcomingTitle))
            },
            HomeExploreSectionUiState(highestRatedTitle, highestRatedState) {
                navController.navigateSafe(highestRatedSelector.toMediaListRoute(highestRatedTitle))
            },
            HomeExploreSectionUiState(mostPopularTitle, mostPopularState) {
                navController.navigateSafe(mostPopularSelector.toMediaListRoute(mostPopularTitle))
            }
        ),
        onItemClick = { media ->
            navController.navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
        },
        onRetry = viewModel::refreshMangaData
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Search
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchDestination(
    navController: NavHostController,
    focusSearch: Boolean
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    // SearchViewModel is activity-scoped so state persists across Search ↔ Facet navigation,
    // matching the original activityViewModel() binding in SearchFragment/FacetFragment.
    val viewModel: SearchViewModel = koinViewModel(
        viewModelStoreOwner = activity
    )

    val searchBoxView = remember { SearchBoxViewCompose() }
    val connectionHandler = remember { ConnectionHandler() }

    val query by searchBoxView.queryFlow.collectAsState()
    val searchType by viewModel.currentSearchType.collectAsStateWithLifecycle()
    val clientStatus by viewModel.searchClientStatus.collectAsStateWithLifecycle()
    val filters by viewModel.filtersLiveData.collectAsStateWithLifecycle()
    val filterCount = filters?.getFilters()?.size ?: 0
    val searchItems = viewModel.searchResultSource.collectAsLazyPagingItems()

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    // Wire the Algolia SearchBoxConnector to our Compose-observable view whenever the client
    // is (re-)created. Also auto-scrolls to top after each new search, mirroring
    // SearchFragment.observeSearchBox().
    val searchBox by viewModel.searchBox.collectAsStateWithLifecycle()
    DisposableEffect(searchBox) {
        searchBox?.let { box ->
            connectionHandler.clear()
            connectionHandler += box.connectView(searchBoxView)
            connectionHandler += SearchResponseListener(box) {
                gridState.requestScrollToItem(0)
                listState.requestScrollToItem(0)
            }
        }
        onDispose { connectionHandler.clear() }
    }

    SearchScreen(
        query = query,
        isSearchFocused = focusSearch,
        onQueryChange = { searchBoxView.notifyQueryChanged(it) },
        onSearchFocusChange = {},
        searchType = searchType ?: io.github.drumber.kitsune.data.presentation.model.algolia.SearchType.Media,
        onSearchTypeChange = { viewModel.switchSearchType(it) },
        clientStatus = clientStatus ?: SearchViewModel.SearchClientStatus.NotInitialized,
        onRetrySearchClient = { viewModel.initializeSearchClient() },
        filterCount = filterCount,
        onFilterClick = { navController.navigateSafe(Routes.Facet) },
        onFilterLongClick = {
            if (!viewModel.filtersLiveData.value?.getFilters().isNullOrEmpty()) {
                viewModel.clearSearchFilter()
            }
        },
        gridState = gridState,
        columnState = listState,
        searchItems = searchItems,
        onMediaClick = { media ->
            navController.navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
        },
        onUserClick = { user ->
            navController.navigateSafe(Routes.UserProfile(userId = user.id, userName = user.name))
        }
    )
}

/**
 * Mirrors [SearchFragment.SearchResponseListener]: scrolls results to top after each new search
 * query is submitted and a response arrives.
 */
private class SearchResponseListener(
    searchBox: SearchBoxConnector<ResponseSearch>,
    private val onSearchReceived: () -> Unit
) : AbstractConnection() {

    private val weakSearchBox = WeakReference(searchBox)
    private var pendingSearch = false

    private val onQueryChanged = { _: Any? -> pendingSearch = true }
    private val onSearchResponse = { response: ResponseSearch? ->
        if (pendingSearch) onSearchReceived()
        if (pendingSearch && response?.pageOrNull == 0) pendingSearch = false
    }

    override fun connect() {
        super.connect()
        weakSearchBox.get()?.let {
            it.viewModel.query.subscribe(onQueryChanged)
            it.searcher.response.subscribe(onSearchResponse)
        }
    }

    override fun disconnect() {
        super.disconnect()
        weakSearchBox.get()?.let {
            it.viewModel.query.unsubscribe(onQueryChanged)
            it.searcher.response.unsubscribe(onSearchResponse)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Facet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FacetDestination(navController: NavHostController) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    val viewModel: SearchViewModel = koinViewModel(
        viewModelStoreOwner = activity
    )

    val connection = remember { ConnectionHandler() }

    val kindState = remember { FacetListViewState() }
    val seasonState = remember { FacetListViewState() }
    val subtypeState = remember { FacetListViewState() }
    val streamersState = remember { FacetListViewState() }
    val ageRatingState = remember { FacetListViewState() }
    val yearState = remember { NumberRangeViewState() }
    val avgRatingState = remember { NumberRangeViewState() }

    val clientStatus by viewModel.searchClientStatus.collectAsStateWithLifecycle()
    val filters by viewModel.filtersLiveData.collectAsStateWithLifecycle()
    val filterCount = filters?.getFilters()?.size ?: 0
    var categoriesCount by remember { mutableIntStateOf(KitsunePref.searchCategories.size) }
    LifecycleResumeEffect(Unit) {
        categoriesCount = KitsunePref.searchCategories.size
        onPauseOrDispose { }
    }

    // Connect the Algolia filter facet connectors to the Compose view states, mirroring
    // FacetFragment.observeFilterFacets(). Re-runs when filterFacets changes (client recreation).
    val filterFacets by viewModel.filterFacets.collectAsStateWithLifecycle()
    DisposableEffect(filterFacets) {
        filterFacets?.let { facets ->
            connection.clear()
            connection += facets.kindConnector.connectView(kindState, facets.kindPresenter)
            connection += facets.yearConnector.connectView(yearState)
            connection += facets.avgRatingConnector.connectView(avgRatingState)
            connection += facets.seasonConnector.connectView(seasonState, facets.seasonPresenter)
            connection += facets.subtypeConnector.connectView(subtypeState, facets.subtypePresenter)
            connection += facets.streamersConnector.connectView(streamersState, facets.streamersPresenter)
            connection += facets.ageRatingConnector.connectView(ageRatingState, facets.ageRatingPresenter)
        }
        onDispose { connection.clear() }
    }

    FacetScreen(
        clientStatus = clientStatus ?: SearchViewModel.SearchClientStatus.NotInitialized,
        onRetrySearchClient = { viewModel.initializeSearchClient() },
        filterCount = filterCount,
        onResetFilter = { viewModel.clearSearchFilter() },
        onNavigateUp = { navController.navigateUp() },
        onCategoriesClick = { navController.navigateSafe(Routes.Categories) },
        categoriesCount = categoriesCount,
        kindState = kindState,
        yearState = yearState,
        avgRatingState = avgRatingState,
        seasonState = seasonState,
        subtypeState = subtypeState,
        streamersState = streamersState,
        ageRatingState = ageRatingState
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Categories
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoriesDestination(navController: NavHostController) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    val viewModel: CategoriesViewModel = koinViewModel()
    val searchViewModel: SearchViewModel = koinViewModel(viewModelStoreOwner = activity)

    val rootNodes by viewModel.rootNodes.collectAsStateWithLifecycle()
    val revision by viewModel.revision.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()
    val selected by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()

    val rows = rememberCategoryRows(rootNodes, revision, expandedIds, selected)

    val dismiss = {
        viewModel.storeSelectedCategories()
        searchViewModel.updateCategoryFilters()
        navController.navigateUp()
        Unit
    }

    BackHandler { dismiss() }

    CategoriesScreen(
        rows = rows,
        isLoading = isLoading,
        hasError = hasError,
        onRetry = { viewModel.fetchChildCategories(null) },
        onDismiss = dismiss,
        onUnselectAll = { viewModel.clearSelectedCategories() },
        onToggleExpand = { viewModel.toggleExpanded(it.node) },
        onToggleSelection = { row, isSelected ->
            viewModel.setCategorySelected(row.wrapper, isSelected)
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// MediaList
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaListDestination(navController: NavHostController, route: Routes.MediaList) {
    val viewModel: MediaListViewModel = koinViewModel()
    val mediaSelector = remember(route) { route.toMediaSelector() }

    LaunchedEffect(mediaSelector) {
        viewModel.setMediaSelector(mediaSelector)
    }

    @Suppress("UNCHECKED_CAST")
    val items = (viewModel.dataSource as kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<Media>>)
        .collectAsLazyPagingItems()

    val gridState = rememberLazyGridState()
    val appBarState = rememberTopAppBarState()

    val itemWidth = dimensionResource(KitsunePref.mediaItemSize.widthRes)
    val itemHeight = dimensionResource(KitsunePref.mediaItemSize.heightRes)
    val itemMargin = dimensionResource(R.dimen.media_item_margin)
    val columns = GridCells.Adaptive(itemWidth + itemMargin * 2)
    val itemAspectRatio = itemWidth / itemHeight

    MediaListScreen(
        title = route.title,
        items = items,
        columns = columns,
        itemAspectRatio = itemAspectRatio,
        gridState = gridState,
        topAppBarState = appBarState,
        onNavigateUp = { navController.navigateUp() },
        onMediaClick = { media ->
            navController.navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Library
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryDestination(navController: NavHostController, backStackEntry: NavBackStackEntry) {
    val viewModel: LibraryViewModel = koinViewModel()
    val context = LocalContext.current

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val libraryEntries = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val modifications by viewModel.notSynchronizedLibraryEntryModifications.collectAsStateWithLifecycle()
    val offlineSyncCount = modifications?.size ?: 0
    val localUser by viewModel.localUser.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Keep the paging refresh callback wired and clean up on exit, matching the SideEffect +
    // onDestroyView pattern from LibraryFragment.
    SideEffect { viewModel.doRefreshListener = { libraryEntries.refresh() } }
    DisposableEffect(Unit) { onDispose { viewModel.doRefreshListener = null } }

    // Show snackbar on library update errors.
    val errorUpdateFailed = stringResource(R.string.error_library_update_failed)
    val errorUpdateNotFound = stringResource(R.string.error_library_update_not_found)
    LaunchedEffect(Unit) {
        viewModel.libraryChangeResultFlow.collect { result ->
            val msg = libraryChangeSnackbarMessage(result, context, errorUpdateFailed, errorUpdateNotFound)
            if (msg != null) snackbarHostState.showSnackbar(msg)
        }
    }

    // Debounced auto-sync when unsynced modifications appear and the network is unmetered,
    // replacing the Debouncer(5000L) from LibraryFragment.onViewCreated.
    LaunchedEffect(modifications) {
        if (!viewModel.hasUser()) return@LaunchedEffect
        viewModel.invalidatePagingSource()
        delay(5_000L)
        val mods = modifications ?: return@LaunchedEffect
        if (mods.isNotEmpty()) {
            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (!cm.isActiveNetworkMetered) {
                viewModel.synchronizeOfflineLibraryUpdates()
            }
        }
    }

    // Consume the "entry updated" nav result from LibraryEditEntry and refresh the list.
    backStackEntry.NavResultEffect<Boolean>(NavResults.LIBRARY_ENTRY_UPDATED) {
        viewModel.triggerAdapterUpdate()
    }

    // Rating bottom sheet state — replaces the RatingBottomSheet dialog-fragment (plan decision 4).
    var ratingSheetEntry by remember { mutableStateOf<LibraryEntryWithModification?>(null) }
    val ratingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // DB-request dialog state.
    var showDbDialog by remember { mutableStateOf(false) }

    if (showDbDialog) {
        DbRequestDialog(
            onDismiss = { showDbDialog = false },
            onUrlSelected = { url ->
                showDbDialog = false
                navController.navigateSafe(Routes.WebView(url))
            }
        )
    }

    ratingSheetEntry?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { ratingSheetEntry = null },
            sheetState = ratingSheetState
        ) {
            RatingScreen(
                title = item.media?.title ?: "",
                ratingTwenty = item.ratingTwenty?.takeIf { it != -1 },
                ratingSystem = RatingSystemUtil.getRatingSystem(),
                onRate = { rating ->
                    viewModel.lastRatedLibraryEntry = item.libraryEntry
                    viewModel.updateRating(rating)
                    ratingSheetEntry = null
                },
                onRemoveRating = {
                    viewModel.lastRatedLibraryEntry = item.libraryEntry
                    viewModel.updateRating(null)
                    ratingSheetEntry = null
                },
                onDismiss = { ratingSheetEntry = null }
            )
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
            if (current.contains(status)) current.remove(status) else current.add(status)
            viewModel.setLibraryEntryStatus(current)
        },
        onSyncClicked = { viewModel.synchronizeOfflineLibraryUpdates() },
        onDbRequestClicked = { showDbDialog = true },
        onLoginClicked = {
            navController.navigateSafe(Routes.Login())
        },
        onEntryClicked = { item ->
            val media = item.libraryEntry.media ?: return@LibraryScreen
            navController.navigateSafe(Routes.Details(mediaId = media.id, isAnime = media is Anime))
        },
        onEntryLongClicked = { item ->
            navController.navigateSafe(Routes.LibraryEditEntry(libraryEntryId = item.libraryEntry.id))
        },
        onEpisodeWatched = { viewModel.markEpisodeWatched(it) },
        onEpisodeUnwatched = { viewModel.markEpisodeUnwatched(it) },
        onRatingClicked = { item -> ratingSheetEntry = item }
    )
}

@Composable
private fun DbRequestDialog(onDismiss: () -> Unit, onUrlSelected: (String) -> Unit) {
    val options = listOf(
        stringResource(R.string.db_request_anime) to Kitsu.ANIME_DB_REQUEST_URL,
        stringResource(R.string.db_request_open_anime) to Kitsu.OPEN_ANIME_REQUESTS_URL,
        stringResource(R.string.db_request_manga) to Kitsu.MANGA_DB_REQUEST_URL,
        stringResource(R.string.db_request_open_manga) to Kitsu.OPEN_MANGA_REQUESTS_URL
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_db_request)) },
        text = {
            Column {
                options.forEach { (label, url) ->
                    TextButton(onClick = { onUrlSelected(url) }) { Text(label) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}

/** Lifted from [LibraryFragment] so the identical snackbar-message logic is reused here. */
private fun libraryChangeSnackbarMessage(
    result: io.github.drumber.kitsune.ui.library.LibraryChangeResult,
    context: android.content.Context,
    errorUpdateFailed: String,
    errorUpdateNotFound: String
): String? = when (result) {
    is io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibraryUpdateResult ->
        when (val r = result.result) {
            is io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Success -> null
            is io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Failure ->
                when (r.reason) {
                    is io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NotFound -> errorUpdateNotFound
                    else -> errorUpdateFailed
                }
        }
    is io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibrarySynchronizationResult -> {
        val failedCount = result.results.count {
            it !is io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Success
        }
        when {
            failedCount == 0 -> null
            failedCount == 1 -> errorUpdateFailed
            else -> context.getString(R.string.error_library_update_failed_multiple, failedCount)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LibraryEditEntry
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryEditEntryDestination(navController: NavHostController, libraryEntryId: String) {
    val viewModel: LibraryEditEntryViewModel = koinViewModel()

    // Initialise the entry once (guards against repeated calls via LibraryEditEntryViewModel).
    LaunchedEffect(libraryEntryId) {
        viewModel.initLibraryEntry(libraryEntryId)
    }

    // When the save/delete operation completes, report the update to Library and pop.
    val loadState by viewModel.loadState.collectAsStateWithLifecycle()
    LaunchedEffect(loadState) {
        if (loadState == LibraryEditEntryViewModel.LoadState.CloseDialog) {
            navController.setNavResult(NavResults.LIBRARY_ENTRY_UPDATED, true)
            navController.popBackStack()
        }
    }

    // Current entry values (needed for date pickers and the rating sheet).
    val wrapper by viewModel.libraryEntryWithModification.collectAsStateWithLifecycle()

    // Rating bottom sheet state.
    var showRatingSheet by remember { mutableStateOf(false) }
    var datePickerRequest by remember { mutableStateOf<DatePickerRequest?>(null) }
    val ratingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val startedDatePickerTitle = stringResource(R.string.library_edit_started)
    val finishedDatePickerTitle = stringResource(R.string.library_edit_finished)

    datePickerRequest?.let { request ->
        DatePickerRequestDialog(
            request = request,
            onDismiss = { datePickerRequest = null }
        )
    }

    if (showRatingSheet) {
        val entry = wrapper
        if (entry != null) {
            ModalBottomSheet(
                onDismissRequest = { showRatingSheet = false },
                sheetState = ratingSheetState
            ) {
                RatingScreen(
                    title = entry.media?.title ?: "",
                    ratingTwenty = entry.ratingTwenty?.takeIf { it != -1 },
                    ratingSystem = RatingSystemUtil.getRatingSystem(),
                    onRate = { rating ->
                        viewModel.updateLibraryEntry { it.copy(ratingTwenty = rating) }
                        showRatingSheet = false
                    },
                    onRemoveRating = {
                        // -1 signals "remove" only when there was a prior server-side rating;
                        // null means no rating was ever set (matches the Fragment's logic).
                        val oldRating = viewModel.uneditedLibraryEntryWrapper?.ratingTwenty
                        val ratingToSet = if (oldRating == null) null else -1
                        viewModel.updateLibraryEntry { it.copy(ratingTwenty = ratingToSet) }
                        showRatingSheet = false
                    },
                    onDismiss = { showRatingSheet = false }
                )
            }
        }
    }

    LibraryEditEntryScreen(
        onDismiss = { navController.popBackStack() },
        onOpenStartedDatePicker = {
            val entry = wrapper ?: return@LibraryEditEntryScreen
            val today = todayUtcMillis()
            val selection = entry.startedAt?.parseUtcDate()?.time?.stripTimeUtcMillis() ?: today
            val finished = entry.finishedAt?.parseUtcDate()?.time?.stripTimeUtcMillis()
            datePickerRequest = DatePickerRequest(
                title = startedDatePickerTitle,
                initialSelection = selection,
                minDateMillis = null,
                maxDateMillis = minOf(finished ?: today, today)
            ) { dateMillis ->
                viewModel.updateLibraryEntry { it.copy(startedAt = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)) }
            }
        },
        onOpenFinishedDatePicker = {
            val entry = wrapper ?: return@LibraryEditEntryScreen
            val today = todayUtcMillis()
            val selection = entry.finishedAt?.parseUtcDate()?.time?.stripTimeUtcMillis() ?: today
            val started = entry.startedAt?.parseUtcDate()?.time?.stripTimeUtcMillis()
            datePickerRequest = DatePickerRequest(
                title = finishedDatePickerTitle,
                initialSelection = selection,
                minDateMillis = started,
                maxDateMillis = today
            ) { dateMillis ->
                viewModel.updateLibraryEntry { it.copy(finishedAt = dateMillis.toDate().formatDate(DATE_FORMAT_ISO)) }
            }
        },
        onShowRatingSheet = { showRatingSheet = true }
    )
}

private data class DatePickerRequest(
    val title: String,
    val initialSelection: Long,
    val minDateMillis: Long?,
    val maxDateMillis: Long,
    val onDateSelected: (Long) -> Unit
)

private fun todayUtcMillis(): Long =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerRequestDialog(
    request: DatePickerRequest,
    onDismiss: () -> Unit
) {
    val selectableDates = remember(request) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= request.maxDateMillis &&
                        (request.minDateMillis == null || utcTimeMillis >= request.minDateMillis)
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = request.initialSelection,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let(request.onDateSelected)
                    onDismiss()
                },
                enabled = state.selectedDateMillis != null
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    text = request.title,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}
