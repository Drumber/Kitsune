package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.filter
import androidx.paging.map
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.FollowMapper.toFollowUser
import io.github.drumber.kitsune.data.source.network.user.FollowNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.FollowPagingDataSource
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import kotlinx.coroutines.flow.map

class FollowRepository(
    private val followNetworkDataSource: FollowNetworkDataSource
) {

    /**
     * Pager for the follow relationships of the user with [userId].
     *
     * For [FollowListType.FOLLOWING] the listed users are the ones the user follows, for
     * [FollowListType.FOLLOWERS] the listed users are the ones following the user.
     */
    fun followListPager(
        userId: String,
        type: FollowListType,
        pageSize: Int = Kitsu.DEFAULT_PAGE_SIZE
    ) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            FollowPagingDataSource(followNetworkDataSource, buildFilter(userId, type, pageSize))
        }
    ).flow.map { pagingData ->
        pagingData
            .filter { follow -> follow.id != null && follow.listedUser(type)?.id != null }
            .map { follow -> follow.toFollowUser(follow.listedUser(type))!! }
    }

    private fun buildFilter(userId: String, type: FollowListType, pageSize: Int): Filter {
        val filter = Filter()
            .pageLimit(pageSize)
        return when (type) {
            FollowListType.FOLLOWING -> filter
                .filter("follower", userId)
                .include("followed")
            FollowListType.FOLLOWERS -> filter
                .filter("followed", userId)
                .include("follower")
        }
    }

    private fun NetworkFollow.listedUser(type: FollowListType): NetworkUser? = when (type) {
        FollowListType.FOLLOWING -> followed
        FollowListType.FOLLOWERS -> follower
    }

    /**
     * Returns the id of the follow relationship where [followerId] follows [followedId], or `null`
     * if no such relationship exists.
     */
    suspend fun getFollowId(followerId: String, followedId: String): String? {
        val filter = Filter()
            .filter("follower", followerId)
            .filter("followed", followedId)
            .fields("follows", "id")
            .pageLimit(1)
        return followNetworkDataSource.getFollows(filter)?.firstOrNull()?.id
    }

    /**
     * Creates a follow relationship where [followerId] follows [followedId] and returns the created
     * follow id, or `null` on failure.
     */
    suspend fun follow(followerId: String, followedId: String): String? {
        val follow = NetworkFollow(
            follower = NetworkUser(id = followerId),
            followed = NetworkUser(id = followedId)
        )
        return followNetworkDataSource.createFollow(follow)?.id
    }

    suspend fun unfollow(followId: String): Boolean {
        return followNetworkDataSource.deleteFollow(followId)
    }
}

enum class FollowListType {
    FOLLOWING,
    FOLLOWERS
}
