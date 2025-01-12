package io.github.drumber.kitsune.data.model.testutils

import io.github.drumber.kitsune.data.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.model.library.LibraryStatus
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