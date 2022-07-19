package io.github.drumber.kitsune.util.extensions

import android.content.Intent
import android.net.Uri
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions

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
    requireContext().showSomethingWrongToast()
}

fun Fragment.startUrlShareIntent(url: String, title: String? = null) {
    val shareIntent = Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }, title)
    startActivity(shareIntent)
}

fun Fragment.openCharacterOnMAL(malId: Int) {
    val malCharacterUrl = "https://myanimelist.net/character/$malId"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(malCharacterUrl))
    startActivity(intent)
}
