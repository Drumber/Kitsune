package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.shared.DATE_FORMAT_ISO
import net.datafaker.Faker

fun libraryEntryModification(faker: Faker) = LibraryEntryModification(
    id = faker.number().positive().toString(),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(1, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool()
)

fun localLibraryEntryModification(faker: Faker) = LocalLibraryEntryModification(
    id = faker.number().positive().toString(),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LocalLibraryStatus.entries.random(),
    progress = faker.number().positive(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(1, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool()
)
