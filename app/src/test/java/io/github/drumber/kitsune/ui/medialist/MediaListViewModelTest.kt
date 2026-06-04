package io.github.drumber.kitsune.ui.medialist

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class MediaListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun animeRepository(): AnimeRepository = mock {
        on { animePager(any(), any()) } doReturn flowOf(PagingData.empty<Anime>())
        on { trendingAnimePager(any(), any()) } doReturn flowOf(PagingData.empty<Anime>())
    }

    private fun mangaRepository(): MangaRepository = mock {
        on { mangaPager(any(), any()) } doReturn flowOf(PagingData.empty<Manga>())
        on { trendingMangaPager(any(), any()) } doReturn flowOf(PagingData.empty<Manga>())
    }

    private fun selector(mediaType: MediaType, requestType: RequestType) =
        MediaSelector(
            mediaType = mediaType,
            filterOptions = mutableMapOf(),
            requestType = requestType
        )

    @Test
    fun `dataSource emits nothing before a media selector is set`() = runTest {
        val anime = animeRepository()
        val manga = mangaRepository()
        val vm = MediaListViewModel(anime, manga)

        vm.dataSource.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(anime, never()).animePager(any(), any())
        verify(manga, never()).mangaPager(any(), any())
    }

    @Test
    fun `dataSource uses the anime pager for anime ALL requests`() = runTest {
        val anime = animeRepository()
        val manga = mangaRepository()
        val vm = MediaListViewModel(anime, manga)

        vm.dataSource.test {
            vm.setMediaSelector(selector(MediaType.Anime, RequestType.ALL))
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(anime).animePager(any(), any())
        verify(anime, never()).trendingAnimePager(any(), any())
    }

    @Test
    fun `dataSource uses the trending anime pager for anime TRENDING requests`() = runTest {
        val anime = animeRepository()
        val manga = mangaRepository()
        val vm = MediaListViewModel(anime, manga)

        vm.dataSource.test {
            vm.setMediaSelector(selector(MediaType.Anime, RequestType.TRENDING))
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(anime).trendingAnimePager(any(), any())
        verify(anime, never()).animePager(any(), any())
    }

    @Test
    fun `dataSource uses the manga pager for manga ALL requests`() = runTest {
        val anime = animeRepository()
        val manga = mangaRepository()
        val vm = MediaListViewModel(anime, manga)

        vm.dataSource.test {
            vm.setMediaSelector(selector(MediaType.Manga, RequestType.ALL))
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(manga).mangaPager(any(), any())
        verify(manga, never()).trendingMangaPager(any(), any())
    }

    @Test
    fun `dataSource uses the trending manga pager for manga TRENDING requests`() = runTest {
        val anime = animeRepository()
        val manga = mangaRepository()
        val vm = MediaListViewModel(anime, manga)

        vm.dataSource.test {
            vm.setMediaSelector(selector(MediaType.Manga, RequestType.TRENDING))
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(manga).trendingMangaPager(any(), any())
        verify(manga, never()).mangaPager(any(), any())
    }
}
