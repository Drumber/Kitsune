package io.github.drumber.kitsune.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.View
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

fun Activity.setLightStatusBar() {
    // API 30 still uses deprecated 'systemUiVisibility' if 'android:windowLightStatusBar' is set in themes.xml
    // see: https://stackoverflow.com/a/68935230/12821118
    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    } else*/ if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.apply {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Activity.clearLightStatusBar() {
    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    } else*/ if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.apply {
            systemUiVisibility = systemUiVisibility xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Activity.isLightStatusBar(): Boolean {
    return when {
        /*Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            window.insetsController?.systemBarsAppearance?.and(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS) != 0
        }*/
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            val flag = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            flag != 0
        }
        else -> {
            false
        }
    }
}

inline fun Context.isNightMode(): Boolean {
    return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

fun Resources.Theme.getColor(resid: Int): Int {
    val typedValue = TypedValue()
    this.resolveAttribute(resid, typedValue, true)
    return typedValue.data
}

fun String.toDate(format: String): Calendar {
    val date = SimpleDateFormat(format).parse(this)
    return Calendar.getInstance().apply {
        time = date
    }
}
