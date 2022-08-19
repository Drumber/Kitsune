package io.github.drumber.kitsune.util.extensions

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import java.text.NumberFormat

/**
 * Make the internal RecyclerView of ViewPager2 accessible.
 */
val ViewPager2.recyclerView: RecyclerView
    get() = this[0] as RecyclerView

fun Throwable.showErrorSnackback(view: View): Snackbar {
    val snackbar = Snackbar.make(view, "Error: ${this.message}", Snackbar.LENGTH_LONG)
    // solve snackbar misplacement (remove bottom margin)
    snackbar.view.initMarginWindowInsetsListener(left = true, right = true)
    snackbar.show()
    return snackbar
}

fun SwipeRefreshLayout.setAppTheme() {
    setProgressBackgroundColorSchemeColor(
        ElevationOverlayProvider(context).compositeOverlayWithThemeSurfaceColorIfNeeded(8.0f)
    )
    setColorSchemeColors(context.theme.getColor(R.attr.colorPrimary))
}

fun TextView.setMaxLinesFitHeight() {
    post {
        val maxLines = (height / (lineHeight + lineSpacingExtra)).toInt()
        setMaxLines(maxLines)
    }
}

/**
 * Format double using default locale format.
 */
fun Double.format() = NumberFormat.getInstance().format(this)

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
