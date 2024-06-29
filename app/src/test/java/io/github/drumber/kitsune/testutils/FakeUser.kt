package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.common.user.UserThemePreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkRatingSystemPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkSfwFilterPreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkTitleLanguagePreference
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker

fun networkUser(faker: Faker) = NetworkUser(
    id = faker.number().positive().toString(),
    createdAt = faker.date().birthday(DATE_FORMAT_ISO),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    name = faker.name().name(),
    slug = faker.internet().slug(),
    email = faker.internet().emailAddress(),
    title = faker.name().title(),
    avatar = newImage(faker),
    coverImage = newImage(faker),
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
)