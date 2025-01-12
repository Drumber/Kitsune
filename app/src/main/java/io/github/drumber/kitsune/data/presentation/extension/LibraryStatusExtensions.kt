package io.github.drumber.kitsune.data.presentation.extension

import androidx.annotation.StringRes
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryStatus

@StringRes
fun LibraryStatus.getStringResId(isAnime: Boolean = true) = when (this) {
    LibraryStatus.Completed -> R.string.library_status_completed
    LibraryStatus.Current -> if (isAnime) R.string.library_status_watching else R.string.library_status_reading
    LibraryStatus.Dropped -> R.string.library_status_dropped
    LibraryStatus.OnHold -> R.string.library_status_on_hold
    LibraryStatus.Planned -> if (isAnime) R.string.library_status_planned else R.string.library_status_planned_manga
}
