package io.github.drumber.kitsune.data.presentation.model.user

data class Favorite(
    val id: String,
    val favRank: Int?,
    val item: FavoriteItem?,
    val user: User?
)

interface FavoriteItem
