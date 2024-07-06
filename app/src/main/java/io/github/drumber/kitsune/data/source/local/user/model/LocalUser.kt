package io.github.drumber.kitsune.data.source.local.user.model

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.user.UserThemePreference
import io.github.drumber.kitsune.data.source.local.character.LocalCharacter

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

    val waifu: LocalCharacter?
) {
    companion object {
        fun empty(id: String) = LocalUser(
            id = id,
            createdAt = null,
            name = null,
            slug = null,
            email = null,
            title = null,
            avatar = null,
            coverImage = null,
            about = null,
            location = null,
            gender = null,
            birthday = null,
            waifuOrHusbando = null,
            country = null,
            language = null,
            timeZone = null,
            theme = null,
            sfwFilter = null,
            ratingSystem = null,
            sfwFilterPreference = null,
            titleLanguagePreference = null,
            waifu = null
        )
    }
}
