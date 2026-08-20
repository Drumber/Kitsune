package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.presentation.model.user.FollowUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

object FollowMapper {

    /**
     * Maps a [NetworkFollow] to a [FollowUser] that represents the [user] participating in the
     * follow relationship (either the follower or the followed user, depending on the list).
     * Returns `null` if required data (the follow id or the user id) is missing.
     */
    fun NetworkFollow.toFollowUser(user: NetworkUser?): FollowUser? {
        val followId = id ?: return null
        val userId = user?.id ?: return null
        return FollowUser(
            followId = followId,
            userId = userId,
            name = user.name,
            slug = user.slug,
            title = user.title,
            avatarUrl = user.avatar?.toImage()?.smallOrHigher()
        )
    }
}
