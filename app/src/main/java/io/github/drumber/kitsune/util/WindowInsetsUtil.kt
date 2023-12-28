package io.github.drumber.kitsune.util

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.appbar.CollapsingToolbarLayout

fun Toolbar.initWindowInsetsListener(consume: Boolean = true) {
    val initialHeight = this.layoutParams.height
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getSystemBarsAndCutoutInsets()
        view.updatePadding(
            top = insets.top,
            left = insets.left,
            right = insets.right
        )
        view.layoutParams.height = initialHeight + insets.top
        if (consume) WindowInsetsCompat.CONSUMED else windowInsets
    }
}

fun CollapsingToolbarLayout.initWindowInsetsListener(consume: Boolean = true) {
    val initialHeight = this.layoutParams.height
    val defaultTitleMarginStart = this.expandedTitleMarginStart
    val defaultTitleMarginEnd = this.expandedTitleMarginStart
    val defaultScrimVisibleHeightTrigger = this.scrimVisibleHeightTrigger
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getSystemBarsAndCutoutInsets()
        view.layoutParams.height = initialHeight + insets.top
        this.scrimVisibleHeightTrigger = defaultScrimVisibleHeightTrigger + insets.top

        val isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
        this.expandedTitleMarginStart =
            defaultTitleMarginStart + if (isRtl) insets.right else insets.left
        this.expandedTitleMarginEnd =
            defaultTitleMarginEnd + if (isRtl) insets.left else insets.right
        if (consume) WindowInsetsCompat.CONSUMED else windowInsets
    }
}

fun View.initPaddingWindowInsetsListener(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    consume: Boolean = true
) {
    val (initialLeft, initialTop, initialRight, initialBottom) = listOf(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getSystemBarsAndCutoutInsets()
        view.updatePadding(
            left = if (left) insets.left + initialLeft else paddingLeft,
            top = if (top) insets.top + initialTop else paddingTop,
            right = if (right) insets.right + initialRight else paddingRight,
            bottom = if (bottom) insets.bottom + initialBottom else paddingBottom
        )
        if (consume) WindowInsetsCompat.CONSUMED else windowInsets
    }
}

fun View.initMarginWindowInsetsListener(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    consume: Boolean = true
) {
    val (initialLeft, initialTop, initialRight, initialBottom) = listOf(
        marginLeft,
        marginTop,
        marginRight,
        marginBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getSystemBarsAndCutoutInsets()
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            if (left) leftMargin = insets.left + initialLeft
            if (top) topMargin = insets.top + initialTop
            if (right) rightMargin = insets.right + initialRight
            if (bottom) bottomMargin = insets.bottom + initialBottom
        }
        if (consume) WindowInsetsCompat.CONSUMED else windowInsets
    }
}

fun WindowInsetsCompat.getSystemBarsAndCutoutInsets() = getInsets(
    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
)
