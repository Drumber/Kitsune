package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.source.network.CursorPageData
import io.github.drumber.kitsune.data.source.network.feed.FeedNetworkDataSource
import io.github.drumber.kitsune.data.source.network.feed.FeedPagingDataSource
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkActivityGroup
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FeedRepository(
    private val feedNetworkDataSource: FeedNetworkDataSource,
    private val postInteractionRepository: PostInteractionRepository,
    private val postInteractionStore: PostInteractionStore,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope
) {

    fun globalFeedPager(pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) = feedPager(pageSize) { cursor ->
        feedNetworkDataSource.getGlobalFeed(buildFilter(pageSize, cursor))
    }

    fun timelineFeedPager(userId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            feedNetworkDataSource.getTimelineFeed(userId, buildFilter(pageSize, cursor))
        }

    /**
     * Pager for a single user's profile feed, showing posts they authored as well as posts other
     * users made on their wall (posts targeted at them).
     */
    fun userFeedPager(userId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            feedNetworkDataSource.getUserFeed(userId, buildFilter(pageSize, cursor))
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

    /**
     * Pager for the activity feed of a single media unit (episode for anime or chapter for manga).
     */
    fun mediaUnitFeedPager(isEpisode: Boolean, unitId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            when (isEpisode) {
                true -> feedNetworkDataSource.getMediaEpisodeFeed(unitId, buildFilter(pageSize, cursor))
                false -> feedNetworkDataSource.getMediaChapterFeed(unitId, buildFilter(pageSize, cursor))
            }
        }

    /**
     * Pager for a single group's activity feed, showing the posts published in the group.
     */
    fun groupFeedPager(groupId: String, pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE) =
        feedPager(pageSize) { cursor ->
            feedNetworkDataSource.getGroupFeed(groupId, buildFilter(pageSize, cursor))
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
            FeedPagingDataSource(loadPage) { posts ->
                // Resolve like state on the load path so the like icon is correct on first paint,
                // but resolve liker avatars off it so the feed renders without waiting on them.
                preloadLikeStates(posts)
                externalScope.launch { preloadLikerAvatars(posts) }
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toPost() }
    }

    /**
     * Resolves the current user's like state for every post on a freshly loaded feed page in a
     * single request and publishes it to [postInteractionStore], so the feed renders the correct
     * like state without the UI having to fetch it per item. No-op when the user is not logged in.
     */
    private suspend fun preloadLikeStates(posts: List<NetworkPost>) {
        val userId = userRepository.localUser.value?.id ?: return
        // Skip posts whose like state was already resolved or changed during this session.
        val unresolved = posts.filter { post ->
            val id = post.id
            id != null && postInteractionStore.get(id)?.isLiked == null
        }
        if (unresolved.isEmpty()) return
        try {
            val likedIds = postInteractionRepository.getMyPostLikeIds(
                unresolved.mapNotNull { it.id },
                userId
            )
            unresolved.forEach { post ->
                val postId = post.id ?: return@forEach
                if (postId in likedIds) {
                    postInteractionStore.setLikeState(postId, true, post.postLikesCount ?: 0)
                }
            }
        } catch (e: Exception) {
            logE("Failed to preload like states for feed page.", e)
        }
    }

    /**
     * Resolves and publishes up to three liker avatar urls for every liked post on a freshly
     * loaded feed page to [postInteractionStore]. Runs off the paging load path so the feed
     * renders immediately and avatars appear as they resolve. Because the post-likes endpoint caps
     * a page at 20 results, avatars are resolved per post rather than in a single batched request.
     */
    private suspend fun preloadLikerAvatars(posts: List<NetworkPost>) {
        val targets = posts.filter { post ->
            val id = post.id
            (post.postLikesCount ?: 0) > 0 && id != null &&
                postInteractionStore.get(id)?.likerAvatars == null
        }
        if (targets.isEmpty()) return
        coroutineScope {
            targets.forEach { post ->
                launch {
                    val id = post.id ?: return@launch
                    try {
                        val avatars = postInteractionRepository.getTopLikerAvatars(id)
                        if (avatars.isNotEmpty()) {
                            postInteractionStore.setLikerAvatars(id, avatars)
                        }
                    } catch (e: Exception) {
                        logE("Failed to preload liker avatars for post '$id'.", e)
                    }
                }
            }
        }
    }

    private fun buildFilter(pageSize: Int, cursor: String?) = Filter()
        .filter("kind", "posts")
        .include(
            "subject",
            "subject.user",
            "subject.media",
            "subject.spoiled_unit",
            "subject.uploads",
            "subject.post",
            "subject.post.user",
            "subject.post.media",
            "subject.post.spoiled_unit",
            "subject.post.uploads",
            "actor"
        )
        .fields("users", "avatar", "name", "slug", "title")
        .pageLimit(pageSize)
        .apply { cursor?.let { pageCursor(it) } }

}
