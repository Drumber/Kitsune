package io.github.drumber.kitsune.util.extensions

import android.content.res.Resources
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.elevation.ElevationOverlayProvider
import io.github.drumber.kitsune.R
import java.text.NumberFormat

/**
 * Make the internal RecyclerView of ViewPager2 accessible.
 */
val ViewPager2.recyclerView: RecyclerView
    get() = this[0] as RecyclerView

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
fun Double.format(): String = NumberFormat.getInstance().format(this)

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toPx() = this * Resources.getSystem().displayMetrics.density
