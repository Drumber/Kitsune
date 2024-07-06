package io.github.drumber.kitsune.testutils

import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacter
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacterRole
import io.github.drumber.kitsune.data.source.local.character.LocalCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkMediaCharacter
import io.github.drumber.kitsune.data.source.network.character.model.NetworkMediaCharacterRole
import net.datafaker.Faker

fun networkCharacter(faker: Faker) = NetworkCharacter(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    name = faker.name().name(),
    names = mapOf("en" to faker.name().name()),
    otherNames = listOf(faker.name().name(), faker.name().name()),
    malId = faker.number().positive(),
    description = faker.lorem().sentence(),
    image = image(faker),
    mediaCharacters = listOf(
        NetworkMediaCharacter(
            id = faker.number().positive().toString(),
            role = NetworkMediaCharacterRole.entries.random(),
            media = null
        )
    )
)

fun localCharacter(faker: Faker) = LocalCharacter(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    name = faker.name().name(),
    names = mapOf("en" to faker.name().name()),
    otherNames = listOf(faker.name().name(), faker.name().name()),
    malId = faker.number().positive(),
    description = faker.lorem().sentence(),
    image = image(faker)
)

fun character(faker: Faker) = Character(
    id = faker.number().positive().toString(),
    slug = faker.internet().slug(),
    name = faker.name().name(),
    names = mapOf("en" to faker.name().name()),
    otherNames = listOf(faker.name().name(), faker.name().name()),
    malId = faker.number().positive(),
    description = faker.lorem().sentence(),
    image = image(faker),
    mediaCharacters = listOf(
        MediaCharacter(
            id = faker.number().positive().toString(),
            role = MediaCharacterRole.entries.random(),
            media = null
        )
    )
)