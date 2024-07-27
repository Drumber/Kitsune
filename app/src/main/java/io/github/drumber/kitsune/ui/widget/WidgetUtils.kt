package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.os.Build
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.toPx

fun GlanceModifier.applyIf(
    condition: Boolean,
    block: GlanceModifier.() -> GlanceModifier
): GlanceModifier {
    return if (condition) {
        block()
    } else {
        this
    }
}

fun innerCornerRadius(context: Context, fallbackRadius: Int = 20): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.resources.getDimensionPixelSize(android.R.dimen.system_app_widget_inner_radius)
    } else {
        fallbackRadius.toPx()
    }
}

fun GlanceModifier.cornerRadiusCompat(
    backgroundColorProvider: () -> ColorProvider
): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        cornerRadius(android.R.dimen.system_app_widget_inner_radius)
    } else {
        background(
            ImageProvider(R.drawable.widget_rounded_rect),
            colorFilter = ColorFilter.tint(backgroundColorProvider())
        )
    }
}