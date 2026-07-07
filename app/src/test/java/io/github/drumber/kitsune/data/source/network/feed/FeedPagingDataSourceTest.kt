package io.github.drumber.kitsune.data.source.network.feed

import androidx.paging.PagingSource
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.comment.model.NetworkComment
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivity
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeedPagingDataSourceTest {

    private fun group(vararg activities: NetworkActivity) =
        NetworkActivityGroup(
            id = "g-${activities.firstOrNull()?.id}",
            activities = activities.toList()
        )

    private fun activity(id: String, subject: Any?) =
        NetworkActivity(
            id = id,
            subject = subject as? io.github.drumber.kitsune.data.source.network.feed.model.NetworkFeedSubject
        )

    private fun refresh(loadSize: Int = 10) =
        PagingSource.LoadParams.Refresh<String>(
            key = null,
            loadSize = loadSize,
            placeholdersEnabled = false
        )

    private fun append(key: String, loadSize: Int = 10) =
        PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)

    @Test
    fun shouldFlattenActivityGroups_toPosts_withNextCursor() = runTest {
        // given
        val post1 = NetworkPost(id = "1")
        val post2 = NetworkPost(id = "2")
        val pagingSource = FeedPagingDataSource(
            loadPage = {
                CursorPageData(
                    data = listOf(
                        group(activity("a1", post1)),
                        group(activity("a2", post2))
                    ),
                    next = "cursor-2"
                )
            },
            onPostsLoaded = {}
        )

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data.map { it.id }).containsExactly("1", "2")
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo("cursor-2")
    }

    @Test
    fun shouldResolveCommentSubject_toItsPost() = runTest {
        // given
        val post = NetworkPost(id = "10")
        val comment = NetworkComment(id = "c1", post = post)
        val pagingSource = FeedPagingDataSource(
            loadPage = {
                CursorPageData(data = listOf(group(activity("a1", comment))), next = null)
            },
            onPostsLoaded = {}
        )

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data.map { it.id }).containsExactly("10")
    }

    @Test
    fun shouldDeduplicatePosts_byId() = runTest {
        // given the same post appears in two activity groups
        val post = NetworkPost(id = "5")
        val pagingSource = FeedPagingDataSource(
            loadPage = {
                CursorPageData(
                    data = listOf(
                        group(activity("a1", post)),
                        group(activity("a2", post))
                    ),
                    next = null
                )
            },
            onPostsLoaded = {}
        )

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).hasSize(1)
        assertThat(page.data.single().id).isEqualTo("5")
    }

    @Test
    fun shouldIgnoreGroups_withoutResolvablePostSubject() = runTest {
        // given a group whose only activity has no subject
        val post = NetworkPost(id = "7")
        val pagingSource = FeedPagingDataSource(
            loadPage = {
                CursorPageData(
                    data = listOf(
                        group(activity("a1", null)),
                        group(activity("a2", post))
                    ),
                    next = null
                )
            },
            onPostsLoaded = {}
        )

        // when
        val result = pagingSource.load(refresh())

        // then
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data.map { it.id }).containsExactly("7")
    }

    @Test
    fun shouldForwardCursorKey_toLoadPage() = runTest {
        // given
        var receivedCursor: String? = "unset"
        val pagingSource = FeedPagingDataSource(
            loadPage = { cursor ->
                receivedCursor = cursor
                CursorPageData(data = emptyList(), next = null)
            },
            onPostsLoaded = {}
        )

        // when
        pagingSource.load(append(key = "cursor-99"))

        // then
        assertThat(receivedCursor).isEqualTo("cursor-99")
    }

    @Test
    fun shouldReturnError_whenLoadPageThrows() = runTest {
        // given
        val exception = RuntimeException("boom")
        val pagingSource = FeedPagingDataSource(
            loadPage = { throw exception },
            onPostsLoaded = {}
        )

        // when
        val result = pagingSource.load(refresh())

        // then
        val error = result as PagingSource.LoadResult.Error
        assertThat(error.throwable).isEqualTo(exception)
    }
}
