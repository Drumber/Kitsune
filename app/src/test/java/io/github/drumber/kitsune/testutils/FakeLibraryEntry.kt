package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.common.library.ReactionSkip
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker

fun libraryEntry(faker: Faker, hasAnime: Boolean = faker.bool().bool()) = LibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LibraryStatus.Completed,
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = ReactionSkip.Unskipped,
    anime = if (hasAnime) anime(faker) else null,
    manga = if (!hasAnime) manga(faker) else null,
    user = null
)

fun localLibraryEntry(faker: Faker, hasAnime: Boolean = faker.bool().bool()) = LocalLibraryEntry(
    id = faker.number().positive().toString(),
    updatedAt = faker.date().birthday(DATE_FORMAT_ISO),
    startedAt = faker.date().birthday(DATE_FORMAT_ISO),
    finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
    progressedAt = faker.date().birthday(DATE_FORMAT_ISO),
    status = LibraryStatus.Completed,
    progress = faker.number().positive(),
    reconsuming = faker.bool().bool(),
    reconsumeCount = faker.number().positive(),
    volumesOwned = faker.number().positive(),
    ratingTwenty = faker.number().numberBetween(0, 20),
    notes = faker.text().text(),
    privateEntry = faker.bool().bool(),
    reactionSkipped = ReactionSkip.Unskipped,
    anime = if (hasAnime) localAnime(faker) else null,
    manga = if (!hasAnime) localManga(faker) else null
)
