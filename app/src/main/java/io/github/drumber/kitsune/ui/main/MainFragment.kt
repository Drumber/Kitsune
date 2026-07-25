package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.ui.main.MainFragmentViewModel.NavigationAction
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel

class MainFragment : Fragment(), NavigationBarView.OnItemReselectedListener {

    private val viewModel: MainFragmentViewModel by koinNavGraphViewModel(R.id.main_nav_graph)

    /**
     * Emitting to this flow signals the Compose layer to animate the active tab's content
     * back to the top. This is triggered by [onNavigationItemReselected].
     */
    private val scrollToTopEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        MainContent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationAction.collect(::handleNavigationAction)
            }
        }
    }

    @Composable
    private fun MainContent() {
        val pagerState = rememberPagerState { 2 }
        val animeScrollState = rememberScrollState()
        val mangaScrollState = rememberScrollState()
        var isRefreshing by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.reloadFinished.collect { isFinished ->
                if (isFinished) isRefreshing = false
            }
        }

        // Animate the visible tab's content to the top when the nav bar item is reselected.
        LaunchedEffect(Unit) {
            scrollToTopEvents.collect {
                when (pagerState.currentPage) {
                    0 -> animeScrollState.animateScrollTo(0)
                    1 -> mangaScrollState.animateScrollTo(0)
                }
            }
        }

        MainScreen(
            animeExploreScreenContent = { AnimeExploreContent() },
            mangaExploreScreenContent = { MangaExploreContent() },
            isRefreshing = isRefreshing,
            onRefresh = { isAnime ->
                isRefreshing = true
                if (isAnime) viewModel.refreshAnimeData() else viewModel.refreshMangaData()
            },
            onSearchClick = {
                findNavController().navigateSafe(
                    R.id.main_fragment,
                    MainFragmentDirections.actionMainFragmentToSearchFragment(focusSearch = true)
                )
            },
            pagerState = pagerState,
            animeScrollState = animeScrollState,
            mangaScrollState = mangaScrollState
        )
    }

    @Composable
    private fun AnimeExploreContent() {
        val trendingTitle = stringResource(R.string.section_trending)
        val topAiringTitle = stringResource(R.string.section_top_airing_anime)
        val topUpcomingTitle = stringResource(R.string.section_top_upcoming_anime)
        val highestRatedTitle = stringResource(R.string.section_highest_rated_anime)
        val mostPopularTitle = stringResource(R.string.section_most_popular_anime)

        val trendingState by animeSection(MainFragmentViewModel.TRENDING).collectAsStateWithLifecycle()
        val topAiringState by animeSection(MainFragmentViewModel.TOP_AIRING).collectAsStateWithLifecycle()
        val topUpcomingState by animeSection(MainFragmentViewModel.TOP_UPCOMING).collectAsStateWithLifecycle()
        val highestRatedState by animeSection(MainFragmentViewModel.HIGHEST_RATED).collectAsStateWithLifecycle()
        val mostPopularState by animeSection(MainFragmentViewModel.MOST_POPULAR).collectAsStateWithLifecycle()

        val topAiringSelector = animeSelector(MainFragmentViewModel.FILTER_TOP_AIRING_ANIME, RequestType.ALL)
        val topUpcomingSelector = animeSelector(MainFragmentViewModel.FILTER_TOP_UPCOMING_ANIME, RequestType.ALL)
        val highestRatedSelector = animeSelector(MainFragmentViewModel.FILTER_HIGHEST_RATED_ANIME, RequestType.ALL)
        val mostPopularSelector = animeSelector(MainFragmentViewModel.FILTER_MOST_POPULAR_ANIME, RequestType.ALL)

        HomeExploreScreen(
            sections = listOf(
                HomeExploreSectionUiState(trendingTitle, trendingState) {
                    navigateToMediaList(animeSelector(Filter().limit(30), RequestType.TRENDING), trendingTitle)
                },
                HomeExploreSectionUiState(topAiringTitle, topAiringState) {
                    navigateToMediaList(topAiringSelector, topAiringTitle)
                },
                HomeExploreSectionUiState(topUpcomingTitle, topUpcomingState) {
                    navigateToMediaList(topUpcomingSelector, topUpcomingTitle)
                },
                HomeExploreSectionUiState(highestRatedTitle, highestRatedState) {
                    navigateToMediaList(highestRatedSelector, highestRatedTitle)
                },
                HomeExploreSectionUiState(mostPopularTitle, mostPopularState) {
                    navigateToMediaList(mostPopularSelector, mostPopularTitle)
                }
            ),
            onItemClick = ::onItemClick,
            onRetry = viewModel::refreshAnimeData
        )
    }

    @Composable
    private fun MangaExploreContent() {
        val trendingTitle = stringResource(R.string.section_trending)
        val topAiringTitle = stringResource(R.string.section_top_airing_manga)
        val topUpcomingTitle = stringResource(R.string.section_top_upcoming_manga)
        val highestRatedTitle = stringResource(R.string.section_highest_rated_manga)
        val mostPopularTitle = stringResource(R.string.section_most_popular_manga)

        val trendingState by mangaSection(MainFragmentViewModel.TRENDING).collectAsStateWithLifecycle()
        val topAiringState by mangaSection(MainFragmentViewModel.TOP_AIRING).collectAsStateWithLifecycle()
        val topUpcomingState by mangaSection(MainFragmentViewModel.TOP_UPCOMING).collectAsStateWithLifecycle()
        val highestRatedState by mangaSection(MainFragmentViewModel.HIGHEST_RATED).collectAsStateWithLifecycle()
        val mostPopularState by mangaSection(MainFragmentViewModel.MOST_POPULAR).collectAsStateWithLifecycle()

        val topAiringSelector = mangaSelector(MainFragmentViewModel.FILTER_TOP_AIRING_MANGA, RequestType.ALL)
        val topUpcomingSelector = mangaSelector(MainFragmentViewModel.FILTER_TOP_UPCOMING_MANGA, RequestType.ALL)
        val highestRatedSelector = mangaSelector(MainFragmentViewModel.FILTER_HIGHEST_RATED_MANGA, RequestType.ALL)
        val mostPopularSelector = mangaSelector(MainFragmentViewModel.FILTER_MOST_POPULAR_MANGA, RequestType.ALL)

        HomeExploreScreen(
            sections = listOf(
                HomeExploreSectionUiState(trendingTitle, trendingState) {
                    navigateToMediaList(mangaSelector(Filter().limit(30), RequestType.TRENDING), trendingTitle)
                },
                HomeExploreSectionUiState(topAiringTitle, topAiringState) {
                    navigateToMediaList(topAiringSelector, topAiringTitle)
                },
                HomeExploreSectionUiState(topUpcomingTitle, topUpcomingState) {
                    navigateToMediaList(topUpcomingSelector, topUpcomingTitle)
                },
                HomeExploreSectionUiState(highestRatedTitle, highestRatedState) {
                    navigateToMediaList(highestRatedSelector, highestRatedTitle)
                },
                HomeExploreSectionUiState(mostPopularTitle, mostPopularState) {
                    navigateToMediaList(mostPopularSelector, mostPopularTitle)
                }
            ),
            onItemClick = ::onItemClick,
            onRetry = viewModel::refreshMangaData
        )
    }

    private fun handleNavigationAction(navigationAction: NavigationAction) {
        when (navigationAction) {
            is NavigationAction.OpenMediaList -> {
                val action = MainFragmentDirections.actionMainFragmentToMediaListFragment(
                    navigationAction.mediaSelector, navigationAction.title
                )
                findNavController().navigateSafe(R.id.main_fragment, action)
            }

            is NavigationAction.OpenMediaDetails -> {
                // Stage A: Compose source has no View to hand off for a shared-element transition.
                val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(
                    navigationAction.mediaDto
                )
                findNavController().navigateSafe(R.id.main_fragment, action)
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        scrollToTopEvents.tryEmit(Unit)
    }

    @Suppress("UNCHECKED_CAST")
    private fun animeSection(key: String): LiveData<ResponseData<List<Media>>> =
        viewModel.getAnimeExploreLiveData(key) as LiveData<ResponseData<List<Media>>>

    @Suppress("UNCHECKED_CAST")
    private fun mangaSection(key: String): LiveData<ResponseData<List<Media>>> =
        viewModel.getMangaExploreLiveData(key) as LiveData<ResponseData<List<Media>>>

    private fun animeSelector(filter: Filter, type: RequestType) =
        MediaSelector(MediaType.Anime, filter.options, type)

    private fun mangaSelector(filter: Filter, type: RequestType) =
        MediaSelector(MediaType.Manga, filter.options, type)

    private fun onItemClick(media: Media) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigate(NavigationAction.OpenMediaDetails(media.toMediaDto()))
        }
    }

    private fun navigateToMediaList(mediaSelector: MediaSelector, title: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigate(NavigationAction.OpenMediaList(mediaSelector, title))
        }
    }
}