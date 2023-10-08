package io.github.drumber.kitsune.util.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.drumber.kitsune.R

fun Activity.setStatusBarColor(@ColorInt color: Int) {
    if (window.statusBarColor != color)
        window.statusBarColor = color
}

fun Activity.setStatusBarColorRes(@ColorRes colorResource: Int) {
    setStatusBarColor(ContextCompat.getColor(this, colorResource))
}

fun Activity.setLightStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
}

fun Activity.clearLightStatusBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
}

fun Activity.isLightStatusBar(): Boolean {
    return WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars
}

fun Activity.clearLightNavigationBar() {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
}

fun Context.isNightMode(): Boolean {
    return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

fun Resources.Theme.getColor(resid: Int): Int {
    val typedValue = TypedValue()
    this.resolveAttribute(resid, typedValue, true)
    return typedValue.data
}

fun Resources.Theme.getResourceId(resid: Int): Int {
    val typedValue = TypedValue()
    this.resolveAttribute(resid, typedValue, true)
    return typedValue.resourceId
}

fun Context.showSomethingWrongToast() {
    Toast.makeText(this, R.string.error_something_wrong, Toast.LENGTH_SHORT).show()
}
