package io.github.drumber.kitsune.data.source.network.reaction

import androidx.paging.PagingSource
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

/**
 * Tests for [ReactionPagingDataSource], which exercises the shared
 * [io.github.drumber.kitsune.data.source.network.BasePagingDataSource.load] implementation.
 */
@RunWith(RobolectricTestRunner::class)
class ReactionPagingDataSourceTest {

    private fun pageData(
        data: List<NetworkMediaReaction>?,
        prev: Int? = null,
        next: Int? = null
    ) = PageData(data = data, first = null, last = null, prev = prev, next = next)

    private fun refresh(loadSize: Int = 10) =
        PagingSource.LoadParams.Refresh<Int>(key = null, loadSize = loadSize, placeholdersEnabled = false)

    private fun append(key: Int, loadSize: Int = 10) =
        PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)

    @Test
    fun shouldReturnPage_onRefresh_withDefaultOffset() = runTest {
        // given
        val reactions = List(3) { NetworkMediaReaction(id = it.toString()) }
        val dataSource = mock<ReactionNetworkDataSource> {
            onSuspend { getMediaReactions(any()) } doReturn pageData(reactions, prev = null, next = 1)
        }
        val pagingSource = ReactionPagingDataSource(dataSource, Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEqualTo(reactions)
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(1)
    }

    @Test
    fun shouldRequestPage_withRefreshOffsetZero() = runTest {
        // given
        val dataSource = mock<ReactionNetworkDataSource> {
            onSuspend { getMediaReactions(any()) } doReturn pageData(emptyList())
        }
        val pagingSource = ReactionPagingDataSource(dataSource, Filter())

        // when
        pagingSource.load(refresh())

        // then the page offset on the forwarded filter is the default (0)
        val filterArg = argumentCaptor<Filter>()
        verify(dataSource).getMediaReactions(filterArg.capture())
        assertThat(filterArg.firstValue.options["page[offset]"]).isEqualTo("0")
    }

    @Test
    fun shouldRequestPage_withAppendKeyAsOffset() = runTest {
        // given
        val dataSource = mock<ReactionNetworkDataSource> {
            onSuspend { getMediaReactions(any()) } doReturn pageData(emptyList(), prev = 1, next = 3)
        }
        val pagingSource = ReactionPagingDataSource(dataSource, Filter())

        // when
        val result = pagingSource.load(append(key = 2))

        // then the append key is used as the page offset
        val filterArg = argumentCaptor<Filter>()
        verify(dataSource).getMediaReactions(filterArg.capture())
        assertThat(filterArg.firstValue.options["page[offset]"]).isEqualTo("2")
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.prevKey).isEqualTo(1)
        assertThat(page.nextKey).isEqualTo(3)
    }

    @Test
    fun shouldReturnError_whenDataIsNull() = runTest {
        // given
        val dataSource = mock<ReactionNetworkDataSource> {
            onSuspend { getMediaReactions(any()) } doReturn pageData(data = null)
        }
        val pagingSource = ReactionPagingDataSource(dataSource, Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val error = result as PagingSource.LoadResult.Error
        assertThat(error.throwable).isInstanceOf(NoDataException::class.java)
    }

    @Test
    fun shouldReturnError_whenDataSourceThrows() = runTest {
        // given
        val exception = RuntimeException("boom")
        val dataSource = mock<ReactionNetworkDataSource> {
            onSuspend { getMediaReactions(any()) } doThrow exception
        }
        val pagingSource = ReactionPagingDataSource(dataSource, Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        val error = result as PagingSource.LoadResult.Error
        assertThat(error.throwable).isEqualTo(exception)
    }
}
