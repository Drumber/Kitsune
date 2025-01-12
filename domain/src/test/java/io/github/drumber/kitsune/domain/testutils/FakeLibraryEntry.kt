package io.github.drumber.kitsune.domain.testutils

import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.model.library.ReactionSkip
import io.github.drumber.kitsune.data.model.media.Media
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
