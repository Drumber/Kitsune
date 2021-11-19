package io.github.drumber.kitsune.util.extensions

import android.content.Intent
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import io.github.drumber.kitsune.R

/**
 * Checks if the current destination of the back stack is equal to the specified destination id.
 * This avoids simultaneous navigation calls, e.g. when the user clicks on two list items at the same time.
 */
fun NavController.navigateSafe(@IdRes currentNavId: Int, directions: NavDirections, navOptions: NavOptions? = null) {
    if (this.currentDestination?.id == currentNavId) {
        this.navigate(directions, navOptions)
    }
}

fun Fragment.showSomethingWrongToast() {
    Toast.makeText(context, R.string.error_something_wrong, Toast.LENGTH_SHORT).show()
}

fun Fragment.startUrlShareIntent(url: String, title: String? = null) {
    val shareIntent = Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }, title)
    startActivity(shareIntent)
}
