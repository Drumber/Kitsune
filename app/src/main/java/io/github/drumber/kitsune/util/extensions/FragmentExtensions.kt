package io.github.drumber.kitsune.util.extensions

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.annotation.IdRes
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import io.github.drumber.kitsune.ui.details.photoview.PhotoViewActivityDirections

/**
 * Checks if the current destination of the back stack is equal to the specified destination id.
 * This avoids simultaneous navigation calls, e.g. when the user clicks on two list items at the same time.
 */
fun NavController.navigateSafe(
    @IdRes currentNavId: Int,
    directions: NavDirections,
    navOptions: NavOptions? = null
) {
    if (this.currentDestination?.id == currentNavId) {
        this.navigate(directions, navOptions)
    }
}

/**
 * Checks if the current destination of the back stack is equal to the specified destination id.
 * This avoids simultaneous navigation calls, e.g. when the user clicks on two list items at the same time.
 */
fun NavController.navigateSafe(
    @IdRes currentNavId: Int,
    directions: NavDirections,
    navigationExtras: Navigator.Extras
) {
    if (this.currentDestination?.id == currentNavId) {
        this.navigate(directions, navigationExtras)
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

fun Fragment.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Fragment.openCharacterOnMAL(malId: Int) {
    val malCharacterUrl = "https://myanimelist.net/character/$malId"
    openUrl(malCharacterUrl)
}

fun Fragment.openPhotoViewActivity(
    imageUrl: String,
    title: String? = null,
    thumbnailUrl: String? = null,
    sharedElement: View? = null
) {
    val transitionName = sharedElement?.let { ViewCompat.getTransitionName(it) }
    val action = PhotoViewActivityDirections.actionGlobalPhotoViewActivity(
        imageUrl,
        title,
        thumbnailUrl,
        transitionName
    )
    val options = if (sharedElement != null && transitionName != null) {
        ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            sharedElement,
            transitionName
        )
    } else {
        null
    }
    val extras = ActivityNavigatorExtras(options)
    findNavController().navigate(action, extras)
}
