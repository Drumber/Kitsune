package io.github.drumber.kitsune.util

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.*

inline fun Toolbar.initWindowInsetsListener() {
    val initialHeight = this.layoutParams.height
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            top = insets.top,
            left = insets.left,
            right = insets.right
        )
        view.layoutParams.height = initialHeight + insets.top
        WindowInsetsCompat.CONSUMED
    }
}

inline fun View.initPaddingWindowInsetsListener(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {
    val (initialLeft, initialTop, initialRight, initialBottom) = listOf(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = if (left) insets.left + initialLeft else paddingLeft,
            top = if (top) insets.top + initialTop else paddingTop,
            right = if (right) insets.right + initialRight else paddingRight,
            bottom = if (bottom) insets.bottom + initialBottom else paddingBottom
        )
        WindowInsetsCompat.CONSUMED
    }
}

inline fun View.initMarginWindowInsetsListener(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {
    val (initialLeft, initialTop, initialRight, initialBottom) = listOf(
        marginLeft,
        marginTop,
        marginRight,
        marginBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            if (left) leftMargin = insets.left + initialLeft
            if (top) topMargin = insets.top + initialTop
            if (right) rightMargin = insets.right + initialRight
            if (bottom) bottomMargin = insets.bottom + initialBottom
        }
        WindowInsetsCompat.CONSUMED
    }
}
