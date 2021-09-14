package io.github.drumber.kitsune.util

import android.app.Activity
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Activity.setStatusBarColor(@ColorInt color: Int) {
    window.statusBarColor = color
}

fun Activity.setStatusBarColorRes(@ColorRes colorResource: Int) {
    setStatusBarColor(ContextCompat.getColor(this, colorResource))
}
