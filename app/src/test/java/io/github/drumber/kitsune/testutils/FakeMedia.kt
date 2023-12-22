package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.domain.model.common.media.AgeRating
import io.github.drumber.kitsune.domain.model.common.media.AnimeSubtype
import io.github.drumber.kitsune.domain.model.common.media.MangaSubtype
import io.github.drumber.kitsune.domain.model.common.media.RatingFrequencies
import io.github.drumber.kitsune.domain.model.common.media.ReleaseStatus
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.database.LocalAnime
import io.github.drumber.kitsune.domain.model.database.LocalManga
import io.github.drumber.kitsune.domain.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain.model.infrastructure.media.Manga
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker
import java.util.Locale
import java.util.concurrent.TimeUnit

fun anime(faker: Faker) = Anime(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = ratingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.Upcoming,
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    episodeCount = faker.number().positive(),
    episodeLength = faker.number().positive(),
    youtubeVideoId = faker.text().text(5, 10, true),
    subtype = AnimeSubtype.TV,
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

fun localAnime(faker: Faker) = LocalAnime(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = ratingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.Upcoming,
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = dbImage(faker),
    coverImage = dbImage(faker),
    totalLength = faker.number().positive(),
    episodeCount = faker.number().positive(),
    episodeLength = faker.number().positive(),
    youtubeVideoId = faker.text().text(5, 10, true),
    subtype = AnimeSubtype.TV
)

fun manga(faker: Faker) = Manga(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = ratingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.Upcoming,
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    subtype = MangaSubtype.Manga,
    chapterCount = faker.number().positive(),
    volumeCount = faker.number().positive(),
    serialization = faker.book().publisher(),
    categories = null,
    mediaRelationships = null
)

fun localManga(faker: Faker) = LocalManga(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = ratingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.Upcoming,
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = false,
    posterImage = dbImage(faker),
    coverImage = dbImage(faker),
    totalLength = faker.number().positive(),
    subtype = MangaSubtype.Manga,
    chapterCount = faker.number().positive(),
    volumeCount = faker.number().positive(),
    serialization = faker.book().publisher()
)

fun titles(): Titles = buildMap {
    put("en", Faker(Locale.ENGLISH).book().title())
    put("en_jp", Faker(Locale.ENGLISH).book().title())
    put("ja_jp", Faker(Locale.JAPANESE).book().title())
    val faker = Faker()
    repeat(faker.random().nextInt(0, 5)) {
        put(faker.locality().localeString(), faker.book().title())
    }
}

fun ratingFrequencies(faker: Faker) = RatingFrequencies(
    r2 = faker.number().positive().toString(),
    r3 = faker.number().positive().toString(),
    r4 = faker.number().positive().toString(),
    r5 = faker.number().positive().toString(),
    r6 = faker.number().positive().toString(),
    r7 = faker.number().positive().toString(),
    r8 = faker.number().positive().toString(),
    r9 = faker.number().positive().toString(),
    r10 = faker.number().positive().toString(),
    r11 = faker.number().positive().toString(),
    r12 = faker.number().positive().toString(),
    r13 = faker.number().positive().toString(),
    r14 = faker.number().positive().toString(),
    r15 = faker.number().positive().toString(),
    r16 = faker.number().positive().toString(),
    r17 = faker.number().positive().toString(),
    r18 = faker.number().positive().toString(),
    r19 = faker.number().positive().toString(),
    r20 = faker.number().positive().toString()
)
