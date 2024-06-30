package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.CharacterMapper.toCharacter
import io.github.drumber.kitsune.data.mapper.CharacterMapper.toLocalCharacter
import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLink
import io.github.drumber.kitsune.data.mapper.UserStatsMapper.toUserStats
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.data.presentation.model.user.FavoriteItem
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavorite
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFavoriteItem
import io.github.drumber.kitsune.data.source.network.user.model.NetworkRatingSystemPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkSfwFilterPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkTitleLanguagePreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

object UserMapper {

    //********************************************************************************************//
    // From Network
    //********************************************************************************************//

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
        waifu = waifu?.toLocalCharacter()
    )

    fun NetworkUser.toUser() = User(
        id = id.require(),
        createdAt = createdAt,
        name = name,
        slug = slug,
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
        stats = stats?.map { it.toUserStats() },
        favorites = favorites?.map { it.toFavorite() },
        waifu = waifu?.toCharacter(),
        profileLinks = profileLinks?.map { it.toProfileLink() }
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

    fun NetworkFavorite.toFavorite(): Favorite = Favorite(
        id = id.require(),
        favRank = favRank,
        item = item?.toFavoriteItem(),
        user = user?.toUser()
    )

    fun NetworkFavoriteItem.toFavoriteItem(): FavoriteItem = when (this) {
        //is Anime -> toAnime() // TODO
        else -> throw IllegalArgumentException("Unknown favorite item type: ${this.javaClass.name}")
    }

    //********************************************************************************************//
    // From Local
    //********************************************************************************************//

    fun LocalUser.toUser() = User(
        id = id,
        createdAt = createdAt,
        name = name,
        slug = slug,
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
        stats = null,
        favorites = null,
        waifu = waifu?.toCharacter(),
        profileLinks = null
    )

    fun LocalRatingSystemPreference.toNetworkRatingSystemPreference() = when (this) {
        LocalRatingSystemPreference.Advanced -> NetworkRatingSystemPreference.Advanced
        LocalRatingSystemPreference.Regular -> NetworkRatingSystemPreference.Regular
        LocalRatingSystemPreference.Simple -> NetworkRatingSystemPreference.Simple
    }

    fun LocalSfwFilterPreference.toNetworkSfwFilterPreference() = when (this) {
        LocalSfwFilterPreference.SFW -> NetworkSfwFilterPreference.SFW
        LocalSfwFilterPreference.NSFW_SOMETIMES -> NetworkSfwFilterPreference.NSFW_SOMETIMES
        LocalSfwFilterPreference.NSFW_EVERYWHERE -> NetworkSfwFilterPreference.NSFW_EVERYWHERE
    }

    fun LocalTitleLanguagePreference.toNetworkTitleLanguagePreference() = when (this) {
        LocalTitleLanguagePreference.Canonical -> NetworkTitleLanguagePreference.Canonical
        LocalTitleLanguagePreference.Romanized -> NetworkTitleLanguagePreference.Romanized
        LocalTitleLanguagePreference.English -> NetworkTitleLanguagePreference.English
    }
}