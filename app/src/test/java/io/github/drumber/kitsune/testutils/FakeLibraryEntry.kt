package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalReactionSkip
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryStatus
import io.github.drumber.kitsune.data.source.network.library.model.NetworkReactionSkip
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker

fun libraryEntry(faker: Faker, media: Media? = null) = io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = io.github.drumber.kitsune.data.presentation.model.library.ReactionSkip.entries.random(),
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
