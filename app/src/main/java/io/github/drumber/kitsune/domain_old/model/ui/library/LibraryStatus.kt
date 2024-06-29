package io.github.drumber.kitsune.domain_old.model.ui.library

import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus

fun LibraryStatus.getStringResId(isAnime: Boolean = true) = when (this) {
    LibraryStatus.Completed -> R.string.library_status_completed
    LibraryStatus.Current -> if (isAnime) R.string.library_status_watching else R.string.library_status_reading
    LibraryStatus.Dropped -> R.string.library_status_dropped
    LibraryStatus.OnHold -> R.string.library_status_on_hold
    LibraryStatus.Planned -> if (isAnime) R.string.library_status_planned else R.string.library_status_planned_manga
}
