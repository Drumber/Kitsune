package io.github.drumber.kitsune.data.source.local.testutils

import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalReactionSkip
import io.github.drumber.kitsune.shared.DATE_FORMAT_ISO
import net.datafaker.Faker

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
