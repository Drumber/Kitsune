package io.github.drumber.kitsune.util.extensions

import android.content.res.Resources
import android.view.View
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import io.github.drumber.kitsune.util.initMarginWindowInsetsListener
import java.text.SimpleDateFormat
import java.util.*

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

fun String.toDate(format: String = "yyyy-MM-dd"): Calendar {
    val date = SimpleDateFormat(format).parse(this)
    return Calendar.getInstance().apply {
        time = date
    }
}

fun Date.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT): String {
    val format = SimpleDateFormat.getDateInstance(dateFormat)
    return format.format(this)
}

fun Calendar.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT) = this.time.formatDate(dateFormat)

fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
