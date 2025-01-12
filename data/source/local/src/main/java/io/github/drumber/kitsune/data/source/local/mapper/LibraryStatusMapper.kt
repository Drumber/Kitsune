package io.github.drumber.kitsune.data.source.local.mapper

import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus

fun LibraryStatus.toLocalLibraryStatus(): LocalLibraryStatus = when (this) {
    LibraryStatus.Current -> LocalLibraryStatus.Current
    LibraryStatus.Planned -> LocalLibraryStatus.Planned
    LibraryStatus.Completed -> LocalLibraryStatus.Completed
    LibraryStatus.OnHold -> LocalLibraryStatus.OnHold
    LibraryStatus.Dropped -> LocalLibraryStatus.Dropped
}
