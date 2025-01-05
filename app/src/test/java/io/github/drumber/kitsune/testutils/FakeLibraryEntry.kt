package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.common.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.ReactionSkip
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.jsonapi.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.jsonapi.library.model.NetworkLibraryStatus
import io.github.drumber.kitsune.data.source.jsonapi.library.model.NetworkReactionSkip
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalReactionSkip
import io.github.drumber.kitsune.shared.DATE_FORMAT_ISO
import net.datafaker.Faker

fun libraryEntry(faker: Faker, media: Media? = null) = LibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = ReactionSkip.entries.random(),
    media = media
)

fun networkLibraryEntry(faker: Faker, media: NetworkMedia? = null) = NetworkLibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = NetworkLibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = NetworkReactionSkip.entries.random(),
    anime = if (media is NetworkAnime) media else null,
    manga = if (media is NetworkManga) media else null,
    user = null
)

fun localLibraryEntry(faker: Faker, media: LocalLibraryMedia? = null) = LocalLibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LocalLibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = LocalReactionSkip.entries.random(),
    media = media
)
