package io.github.drumber.kitsune.data.source.local.mapper

import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryFilterOptions

fun LibraryFilterOptions.toLocalLibraryFilterOptions() = LocalLibraryFilterOptions(
    mediaType = mediaType.name,
    status = status?.map { it.name },
    sortBy = sortBy?.name,
    sortDirection = sortDirection?.name
)
