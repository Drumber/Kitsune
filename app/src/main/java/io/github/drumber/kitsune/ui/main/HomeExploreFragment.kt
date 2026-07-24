package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
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
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.launch
import org.koin.androidx.navigation.koinNavGraphViewModel

class HomeExploreFragment : Fragment() {

    private val viewModel: MainFragmentViewModel by koinNavGraphViewModel(R.id.main_nav_graph)

    companion object {
        const val BUNDLE_MEDIA_TYPE = "bundle_media_type"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView { HomeExploreContent() }

    @Composable
    private fun HomeExploreContent() {
        val mediaType = arguments?.let {
            BundleCompat.getSerializable(it, BUNDLE_MEDIA_TYPE, MediaType::class.java)
        }
        when (mediaType) {
            MediaType.Anime -> AnimeExploreContent()
            MediaType.Manga -> MangaExploreContent()
            else -> Unit
        }
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
