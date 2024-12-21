package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.source.graphql.type.LibraryEntryStatusEnum

fun LibraryEntryStatusEnum.toLibraryStatus() = when (this) {
    LibraryEntryStatusEnum.CURRENT -> LibraryStatus.Current
    LibraryEntryStatusEnum.PLANNED -> LibraryStatus.Planned
    LibraryEntryStatusEnum.COMPLETED -> LibraryStatus.Completed
    LibraryEntryStatusEnum.ON_HOLD -> LibraryStatus.OnHold
    LibraryEntryStatusEnum.DROPPED -> LibraryStatus.Dropped
    LibraryEntryStatusEnum.UNKNOWN__ -> null
}

fun LibraryStatus.toLibraryEntryStatusEnum() = when (this) {
    LibraryStatus.Current -> LibraryEntryStatusEnum.CURRENT
    LibraryStatus.Planned -> LibraryEntryStatusEnum.PLANNED
    LibraryStatus.Completed -> LibraryEntryStatusEnum.COMPLETED
    LibraryStatus.OnHold -> LibraryEntryStatusEnum.ON_HOLD
    LibraryStatus.Dropped -> LibraryEntryStatusEnum.DROPPED
}