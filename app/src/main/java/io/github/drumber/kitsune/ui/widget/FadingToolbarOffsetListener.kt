package io.github.drumber.kitsune.ui.widget

import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.*
import kotlin.math.abs

class FadingToolbarOffsetListener(
    private val activity: Activity,
    private val toolbar: MaterialToolbar,
    private val expandedColor: Int = ContextCompat.getColor(activity, R.color.white),
    private val collapsedColor: Int = activity.theme.getColor(R.attr.colorOnSurface)
) : AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxOffset = appBarLayout.totalScrollRange
        val percent = abs(verticalOffset.toFloat() / maxOffset) // between 0.0 and 1.0

        // fade toolbar icons from white to colorOnSurface while collapsing the toolbar
        val iconTint = ColorUtils.blendARGB(expandedColor, collapsedColor, percent)
        toolbar.setNavigationIconTint(iconTint)
        toolbar.overflowIcon?.let { overFlowIcon ->
            DrawableCompat.setTint(overFlowIcon, iconTint)
        }
        toolbar.menu.forEach { menuItem ->
            if (menuItem.icon != null) {
                val drawable = menuItem.icon.mutate()
                DrawableCompat.setTint(drawable, iconTint)
            }
        }

        // switch to light status bar in light mode
        if (!activity.isNightMode()) {
            if (percent < 0.5 && activity.isLightStatusBar()) {
                activity.clearLightStatusBar()
            }
            if (percent >= 0.5 && !activity.isLightStatusBar() && !activity.isNightMode()) {
                activity.setLightStatusBar()
            }
        }
    }
}