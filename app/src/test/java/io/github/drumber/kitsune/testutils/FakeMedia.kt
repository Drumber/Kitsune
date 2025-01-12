package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.model.Titles
import io.github.drumber.kitsune.data.model.media.AgeRating
import io.github.drumber.kitsune.data.model.media.AnimeSubtype
import io.github.drumber.kitsune.data.model.media.MangaSubtype
import io.github.drumber.kitsune.data.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnimeSubtype
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkMangaSubtype
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkRatingFrequencies
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkReleaseStatus
import io.github.drumber.kitsune.shared.DATE_FORMAT_ISO
import net.datafaker.Faker
import java.util.Locale
import java.util.concurrent.TimeUnit

fun networkAnime(faker: Faker) = NetworkAnime(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = networkRatingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = NetworkReleaseStatus.entries.random(),
    ageRating = AgeRating.entries.random(),
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    episodeCount = faker.number().positive(),
    episodeLength = faker.number().positive(),
    youtubeVideoId = faker.text().text(5, 10, true),
    subtype = NetworkAnimeSubtype.entries.random(),
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

fun anime(faker: Faker) = Anime(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = newRatingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.entries.random(),
    ageRating = AgeRating.entries.random(),
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    episodeCount = faker.number().positive(),
    episodeLength = faker.number().positive(),
    youtubeVideoId = faker.text().text(5, 10, true),
    subtype = AnimeSubtype.entries.random(),
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

fun networkManga(faker: Faker) = NetworkManga(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = networkRatingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = NetworkReleaseStatus.entries.random(),
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    subtype = NetworkMangaSubtype.entries.random(),
    chapterCount = faker.number().positive(),
    volumeCount = faker.number().positive(),
    serialization = faker.book().publisher(),
    categories = null,
    mediaRelationships = null
)

fun manga(faker: Faker) = Manga(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    description = faker.text().text(),
    titles = titles(),
    canonicalTitle = faker.book().title(),
    abbreviatedTitles = faker.collection({ faker.book().title() }).maxLen(4).build().toList(),
    averageRating = faker.number().positive().toString(),
    ratingFrequencies = newRatingFrequencies(faker),
    userCount = faker.number().positive(),
    favoritesCount = faker.number().positive(),
    popularityRank = faker.number().positive(),
    ratingRank = faker.number().positive(),
    startDate = faker.date().birthday(DATE_FORMAT_ISO),
    endDate = faker.date().birthday(DATE_FORMAT_ISO),
    nextRelease = faker.date().birthday(DATE_FORMAT_ISO),
    tba = faker.date().future(360, TimeUnit.DAYS, "MMMMM yyyy"),
    status = ReleaseStatus.entries.random(),
    ageRating = AgeRating.R,
    ageRatingGuide = faker.text().text(5),
    nsfw = faker.bool().bool(),
    posterImage = image(faker),
    coverImage = image(faker),
    totalLength = faker.number().positive(),
    subtype = MangaSubtype.entries.random(),
    chapterCount = faker.number().positive(),
    volumeCount = faker.number().positive(),
    serialization = faker.book().publisher(),
    categories = null,
    mediaRelationships = null
)

fun networkRatingFrequencies(faker: Faker) = NetworkRatingFrequencies(
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

fun newRatingFrequencies(faker: Faker) = RatingFrequencies(
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

fun titles(): Titles = buildMap {
    put("en", Faker(Locale.ENGLISH).book().title())
    put("en_jp", Faker(Locale.ENGLISH).book().title())
    put("ja_jp", Faker(Locale.JAPANESE).book().title())
    val faker = Faker()
    repeat(faker.random().nextInt(0, 5)) {
        put(faker.locality().localeString(), faker.book().title())
    }
}
