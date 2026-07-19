package io.github.drumber.kitsune.data.presentation.model.user

import io.github.drumber.kitsune.data.common.Image

data class UserSearchResult(
    val id: String,
    val name: String?,
    val slug: String?,
    val title: String?,
    val avatar: Image?,
    val followersCount: Int?
)
