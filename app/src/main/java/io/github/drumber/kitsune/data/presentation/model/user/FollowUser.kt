package io.github.drumber.kitsune.data.presentation.model.user

data class FollowUser(
    val followId: String,
    val userId: String,
    val name: String?,
    val slug: String?,
    val title: String?,
    val avatarUrl: String?
)
