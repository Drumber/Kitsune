package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.mapper.ImageMapper.toLocalImage
import io.github.drumber.kitsune.data.mapper.require
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit

fun MediaUnit.toLocalNextMediaUnit(libraryEntryId: String) = LocalNextMediaUnit(
    unitId = id.require(),
    libraryEntryId = libraryEntryId,
    titles = titles,
    canonicalTitle = canonicalTitle,
    number = number,
    thumbnail = thumbnail?.toLocalImage()
)