package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.source.graphql.GetLibraryEntriesWithNextUnitQuery

fun GetLibraryEntriesWithNextUnitQuery.All.toLibraryEntriesWithNextUnit() = nodes
    ?.filterNotNull()
    ?.map(GetLibraryEntriesWithNextUnitQuery.Node::toLibraryEntryWithNextUnit)

fun GetLibraryEntriesWithNextUnitQuery.Node.toLibraryEntryWithNextUnit() =
    libraryEntryWithNextUnitFragment.toLibraryEntryWithNextUnit()
