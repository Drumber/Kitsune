package io.github.drumber.kitsune.data.source.network.comment

import androidx.paging.PagingSource
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkCommentLike
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CommentPagingDataSourceTest {

    private fun pageData(
        data: List<NetworkComment>?,
        prev: Int? = null,
        next: Int? = null
    ) = PageData(data = data, first = null, last = null, prev = prev, next = next)

    private fun refresh(loadSize: Int = 10) =
        PagingSource.LoadParams.Refresh<Int>(key = null, loadSize = loadSize, placeholdersEnabled = false)

    private fun append(key: Int, loadSize: Int = 10) =
        PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)

    @Test
    fun shouldReturnComments_withoutLikeState_whenUserIdIsNull() = runTest {
        // given
        val comments = listOf(NetworkComment(id = "1"), NetworkComment(id = "2"))
        val dataSource = mock<CommentNetworkDataSource> {
            onSuspend { getComments(any()) } doReturn pageData(comments, next = 1)
        }
        val pagingSource = CommentPagingDataSource(dataSource, userId = null, filter = Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(2)
        assertThat(page.data.map { it.likeId }).containsOnlyNulls()
        assertThat(page.nextKey).isEqualTo(1)
        verify(dataSource, never()).getCommentLikes(any())
    }

    @Test
    fun shouldMarkLikedComments_whenUserIdIsPresent() = runTest {
        // given
        val likedComment = NetworkComment(id = "1")
        val unlikedComment = NetworkComment(id = "2")
        val like = NetworkCommentLike(id = "like-1", comment = likedComment)
        val dataSource = mock<CommentNetworkDataSource> {
            onSuspend { getComments(any()) } doReturn pageData(listOf(likedComment, unlikedComment))
            onSuspend { getCommentLikes(any()) } doReturn listOf(like)
        }
        val pagingSource = CommentPagingDataSource(dataSource, userId = "user-1", filter = Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data.first { it.comment.id == "1" }.likeId).isEqualTo("like-1")
        assertThat(page.data.first { it.comment.id == "2" }.likeId).isNull()
        verify(dataSource).getCommentLikes(any())
    }

    @Test
    fun shouldNotFetchLikes_whenPageIsEmpty() = runTest {
        // given
        val dataSource = mock<CommentNetworkDataSource> {
            onSuspend { getComments(any()) } doReturn pageData(emptyList())
        }
        val pagingSource = CommentPagingDataSource(dataSource, userId = "user-1", filter = Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isEmpty()
        verify(dataSource, never()).getCommentLikes(any())
    }

    @Test
    fun shouldUseAppendKeyAsOffset() = runTest {
        // given
        val dataSource = mock<CommentNetworkDataSource> {
            onSuspend { getComments(any()) } doReturn pageData(emptyList(), prev = 4, next = 6)
        }
        val pagingSource = CommentPagingDataSource(dataSource, userId = null, filter = Filter())

        // when
        val result = pagingSource.load(append(key = 5))

        // then
        val filterArg = argumentCaptor<Filter>()
        verify(dataSource).getComments(filterArg.capture())
        assertThat(filterArg.firstValue.options["page[offset]"]).isEqualTo("5")
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.prevKey).isEqualTo(4)
        assertThat(page.nextKey).isEqualTo(6)
    }

    @Test
    fun shouldReturnError_whenDataSourceThrows() = runTest {
        // given
        val exception = RuntimeException("boom")
        val dataSource = mock<CommentNetworkDataSource> {
            onSuspend { getComments(any()) } doThrow exception
        }
        val pagingSource = CommentPagingDataSource(dataSource, userId = null, filter = Filter())

        // when
        val result = pagingSource.load(refresh())

        // then
        val error = result as PagingSource.LoadResult.Error
        assertThat(error.throwable).isEqualTo(exception)
    }
}
