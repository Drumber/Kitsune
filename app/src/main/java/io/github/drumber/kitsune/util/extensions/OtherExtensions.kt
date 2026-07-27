package io.github.drumber.kitsune.util.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import java.text.NumberFormat

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
}

/**
 * Format double using default locale format.
 */
fun Double.format(): String = NumberFormat.getInstance().format(this)

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toPx() = this * Resources.getSystem().displayMetrics.density
