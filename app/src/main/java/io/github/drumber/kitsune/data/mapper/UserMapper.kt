package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkRatingSystemPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkSfwFilterPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkTitleLanguagePreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

object UserMapper {
    fun NetworkUser.toLocalUser() = LocalUser(
        id = id.require(),
        createdAt = createdAt,
        name = name,
        slug = slug,
        email = email,
        title = title,
        avatar = avatar,
        coverImage = coverImage,
        about = about,
        location = location,
        gender = gender,
        birthday = birthday,
        waifuOrHusbando = waifuOrHusbando,
        country = country,
        language = language,
        timeZone = timeZone,
        theme = theme,
        sfwFilter = sfwFilter,
        ratingSystem = ratingSystem?.toLocalRatingSystemPreference(),
        sfwFilterPreference = sfwFilterPreference?.toLocalSfwFilterPreference(),
        titleLanguagePreference = titleLanguagePreference?.toLocalTitleLanguagePreference(),
        stats = null,
        favorites = null,
        waifu = null,
        profileLinks = null
    )

    fun NetworkRatingSystemPreference.toLocalRatingSystemPreference() = when (this) {
        NetworkRatingSystemPreference.Advanced -> LocalRatingSystemPreference.Advanced
        NetworkRatingSystemPreference.Regular -> LocalRatingSystemPreference.Regular
        NetworkRatingSystemPreference.Simple -> LocalRatingSystemPreference.Simple
    }

    fun NetworkSfwFilterPreference.toLocalSfwFilterPreference() = when (this) {
        NetworkSfwFilterPreference.SFW -> LocalSfwFilterPreference.SFW
        NetworkSfwFilterPreference.NSFW_SOMETIMES -> LocalSfwFilterPreference.NSFW_SOMETIMES
        NetworkSfwFilterPreference.NSFW_EVERYWHERE -> LocalSfwFilterPreference.NSFW_EVERYWHERE
    }

    fun NetworkTitleLanguagePreference.toLocalTitleLanguagePreference() = when (this) {
        NetworkTitleLanguagePreference.Canonical -> LocalTitleLanguagePreference.Canonical
        NetworkTitleLanguagePreference.Romanized -> LocalTitleLanguagePreference.Romanized
        NetworkTitleLanguagePreference.English -> LocalTitleLanguagePreference.English
    }
}