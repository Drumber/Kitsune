package io.github.drumber.kitsune.data.source.local.user.model

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.user.UserThemePreference
import io.github.drumber.kitsune.domain_old.model.infrastructure.character.Character
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.Favorite
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats.Stats

data class LocalUser(
    val id: String,
    val createdAt: String?,

    val name: String?,
    val slug: String?,
    val email: String?,
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
    val theme: UserThemePreference?,

    val sfwFilter: Boolean?,
    val ratingSystem: LocalRatingSystemPreference?,
    val sfwFilterPreference: LocalSfwFilterPreference?,
    val titleLanguagePreference: LocalTitleLanguagePreference?,

    // TODO: update imports
    val stats: List<Stats>?,
    val favorites: List<Favorite>?,
    val waifu: Character?,
    val profileLinks: List<ProfileLink>?,
)
