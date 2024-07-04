package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.data.source.local.character.LocalCharacter
import io.github.drumber.kitsune.data.source.network.algolia.model.search.CharacterSearchResult
import io.github.drumber.kitsune.domain_old.model.infrastructure.character.Character

fun CharacterSearchResult.toCharacter() = Character(
    id = id.toString(),
    slug = slug,
    name = canonicalName,
    image = image?.toImage(),
    description = null,
    malId = null,
    names = null,
    otherNames = null
)

// TODO: only temporary
fun CharacterSearchResult.toLocalCharacter() = LocalCharacter(
    id = id.toString(),
    slug = slug,
    name = canonicalName,
    image = /*image?.toImage()*/ null,
    description = null,
    malId = null,
    names = null,
    otherNames = null
)
