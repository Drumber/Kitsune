package io.github.drumber.kitsune.util

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

fun Activity.setStatusBarColor(@ColorInt color: Int) {
    window.statusBarColor = color
}

fun Activity.setStatusBarColorRes(@ColorRes colorResource: Int) {
    setStatusBarColor(ContextCompat.getColor(this, colorResource))
}

fun Resources.Theme.getColor(resid: Int): Int {
    val typedValue = TypedValue()
    this.resolveAttribute(resid, typedValue, true)
    return typedValue.data
}

fun String.toDate(format: String): Calendar {
    val date = SimpleDateFormat("yyyy-MM-dd").parse(this)
    return Calendar.getInstance().apply {
        time = date
    }
}
