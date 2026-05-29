package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.feed.FeedNetworkDataSource
import io.github.drumber.kitsune.data.source.network.feed.FeedPagingDataSource
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import kotlinx.coroutines.flow.map

class FeedRepository(
    private val feedNetworkDataSource: FeedNetworkDataSource
) {

    fun globalFeedPager(pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) = feedPager(pageSize) { cursor ->
        feedNetworkDataSource.getGlobalFeed(buildFilter(pageSize, cursor))
    }

    fun timelineFeedPager(userId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            feedNetworkDataSource.getTimelineFeed(userId, buildFilter(pageSize, cursor))
        }

    /**
     * Pager for the activity feed of a single media (anime or manga), matching the posts shown
     * on the media page of the Kitsu website. The feed id is composed of the capitalized media
     * type and the media id, e.g. `Anime-1` or `Manga-1`.
     */
    fun mediaFeedPager(isAnime: Boolean, mediaId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            val feedId = "${if (isAnime) "Anime" else "Manga"}-$mediaId"
            feedNetworkDataSource.getMediaFeed(feedId, buildFilter(pageSize, cursor))
        }

    private fun feedPager(
        pageSize: Int,
        loadPage: suspend (cursor: String?) -> CursorPageData<NetworkActivityGroup>
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            FeedPagingDataSource(loadPage)
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toPost() }
    }

    private fun buildFilter(pageSize: Int, cursor: String?) = Filter()
        .filter("kind", "posts")
        .include("subject", "subject.user", "subject.media", "actor")
        .pageLimit(pageSize)
        .apply { cursor?.let { pageCursor(it) } }

}
