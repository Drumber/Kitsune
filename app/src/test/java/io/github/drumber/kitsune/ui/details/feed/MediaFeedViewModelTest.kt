package io.github.drumber.kitsune.ui.details.feed

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.FeedRepository
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaFeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun feedRepository(): FeedRepository = mock {
        on { mediaFeedPager(any(), any(), any()) } doReturn flowOf(PagingData.empty<Post>())
    }

    private fun vm(feedRepository: FeedRepository) = MediaFeedViewModel(
        feedRepository = feedRepository,
        userRepository = mock { on { localUser } doReturn MutableStateFlow(null) },
        getLocalUserId = mock { on { invoke() } doReturn "user-1" },
        postManagementRepository = mock(),
        postInteractionRepository = mock(),
        postInteractionStore = mock(),
        contentRevealStore = mock(),
    )

    @Test
    fun `dataSource requests a pager for the selected media`() = runTest {
        val repository = feedRepository()
        val vm = vm(repository)

        vm.dataSource.test {
            vm.initMediaFeed("media-1", isAnime = true)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository).mediaFeedPager(eq(true), eq("media-1"), any())
    }

    @Test
    fun `dataSource does not request a pager before media is set`() = runTest {
        val repository = feedRepository()
        val vm = vm(repository)

        vm.dataSource.test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository, never()).mediaFeedPager(any(), any(), any())
    }
}
