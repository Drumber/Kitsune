package io.github.drumber.kitsune.data.presentation.model.user

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStats

data class User(
    val id: String,
    val createdAt: String?,

    val name: String?,
    val slug: String?,
    val title: String?,

    val avatar: Image?,
    val coverImage: Image?,

    val about: String?,
    val location: String?,
    val gender: String?,
    val birthday: String?,
    val waifuOrHusbando: String?,

    val country: String?,
    val language: String?,
    val timeZone: String?,

    val stats: List<UserStats>?,
    val favorites: List<Favorite>?,
    val waifu: Character?,
    val profileLinks: List<ProfileLink>?
)
