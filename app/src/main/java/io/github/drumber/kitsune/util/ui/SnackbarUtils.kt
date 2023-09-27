package io.github.drumber.kitsune.util.ui

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener

fun showSnackbar(
    parent: View,
    message: CharSequence,
    duration: Int = Snackbar.LENGTH_LONG
): Snackbar {
    return Snackbar.make(parent, message, duration).apply {
        // fixes unnecessary bottom margin
        view.initMarginWindowInsetsListener(left = true, right = true)
        show()
    }
}

fun showSnackbar(
    parent: View,
    @StringRes stringRes: Int,
    duration: Int = Snackbar.LENGTH_LONG
): Snackbar {
    return showSnackbar(parent, parent.resources.getText(stringRes), duration)
}

fun SynchronizationResult.showSnackbarOnFailure(parent: View): Snackbar? {
    val stringRes = when (this) {
        is SynchronizationResult.Success -> return null
        is SynchronizationResult.Failed -> R.string.error_library_update_failed
        is SynchronizationResult.NotFound -> R.string.error_library_update_not_found
    }
    return showSnackbar(parent, stringRes)
}

fun List<SynchronizationResult>.showSnackbarOnAnyFailure(parent: View): Snackbar? {
    val failedCount = count { it !is SynchronizationResult.Success }
    if (failedCount == 0) return null
    val stringRes = when (failedCount) {
        1 -> R.string.error_library_update_failed
        else -> R.string.error_library_update_failed_multiple
    }
    return showSnackbar(parent, parent.resources.getString(stringRes, failedCount))
}
