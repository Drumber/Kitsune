package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.model.user.UserThemePreference
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavorite
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavoriteItem
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkRatingSystemPreference
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkSfwFilterPreference
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkTitleLanguagePreference
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLinkSite
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalSfwFilterPreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.shared.DATE_FORMAT_ISO
import net.datafaker.Faker

fun networkUser(faker: Faker) = NetworkUser(
    id = faker.number().positive().toString(),
    createdAt = faker.date().birthday(DATE_FORMAT_ISO),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    name = faker.name().name(),
    slug = faker.internet().slug(),
    email = faker.internet().emailAddress(),
    title = faker.name().title(),
    avatar = image(faker),
    coverImage = image(faker),
    about = faker.text().text(),
    location = faker.country().name(),
    gender = faker.gender().types(),
    birthday = faker.date().birthday(DATE_FORMAT_ISO),
    waifuOrHusbando = null,
    followersCount = faker.number().positive(),
    followingCount = faker.number().positive(),
    commentsCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    likesGivenCount = faker.number().positive(),
    reviewsCount = faker.number().positive(),
    likesReceivedCount = faker.number().positive(),
    postsCount = faker.number().positive(),
    ratingsCount = faker.number().positive(),
    mediaReactionsCount = faker.number().positive(),
    country = faker.country().countryCode2(),
    language = faker.locality().localeString(),
    timeZone = null,
    theme = UserThemePreference.Light,
    sfwFilter = false,
    ratingSystem = NetworkRatingSystemPreference.Regular,
    shareToGlobal = null,
    sfwFilterPreference = NetworkSfwFilterPreference.SFW,
    titleLanguagePreference = NetworkTitleLanguagePreference.English,
    profileCompleted = true,
    feedCompleted = true,
    proTier = null,
    proExpiresAt = null,
    aoPro = null,
    facebookId = null,
    confirmed = null,
    status = null,
    hasPassword = true,
    subscribedToNewsletter = false,
    stats = null,
    favorites = listOf(networkFavorite(faker, null), networkFavorite(faker, null)),
    waifu = networkCharacter(faker),
    profileLinks = listOf(networkProfileLink(faker, null))
)

fun localUser(faker: Faker) = LocalUser(
    id = faker.number().positive().toString(),
    createdAt = faker.date().birthday(DATE_FORMAT_ISO),
    name = faker.name().name(),
    slug = faker.internet().slug(),
    email = faker.internet().emailAddress(),
    title = faker.name().title(),
    avatar = image(faker),
    coverImage = image(faker),
    about = faker.text().text(),
    location = faker.country().name(),
    gender = faker.gender().types(),
    birthday = faker.date().birthday(DATE_FORMAT_ISO),
    waifuOrHusbando = null,
    country = faker.country().countryCode2(),
    language = faker.locality().localeString(),
    timeZone = null,
    theme = UserThemePreference.Light,
    sfwFilter = false,
    ratingSystem = LocalRatingSystemPreference.Regular,
    sfwFilterPreference = LocalSfwFilterPreference.SFW,
    titleLanguagePreference = LocalTitleLanguagePreference.English,
    waifu = localCharacter(faker),
)

fun networkFavorite(
    faker: Faker,
    user: NetworkUser? = networkUser(faker),
    item: NetworkFavoriteItem? = networkAnime(faker)
) = NetworkFavorite(
    id = faker.number().positive().toString(),
    favRank = faker.number().positive(),
    item = item,
    user = user
)

fun networkProfileLink(faker: Faker, user: NetworkUser? = networkUser(faker)) = NetworkProfileLink(
    id = faker.number().positive().toString(),
    url = faker.internet().url(),
    profileLinkSite = NetworkProfileLinkSite(
        id = faker.number().positive().toString(),
        name = faker.company().name()
    ),
    user = user
)
