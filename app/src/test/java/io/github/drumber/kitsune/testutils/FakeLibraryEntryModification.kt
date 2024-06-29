package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.ui.library.LibraryEntryModification
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker

fun libraryEntryModification(faker: Faker) = LibraryEntryModification(
    id = faker.number().positive().toString(),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LibraryStatus.Completed,
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
    status = LibraryStatus.Completed,
    progress = faker.number().positive(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(1, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool()
)
