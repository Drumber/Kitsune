package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.user.FollowNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class FollowRepository(
    private val followNetworkDataSource: FollowNetworkDataSource
) {

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
