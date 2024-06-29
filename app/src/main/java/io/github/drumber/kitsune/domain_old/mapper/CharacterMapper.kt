package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.domain_old.model.infrastructure.algolia.search.CharacterSearchResult
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
