package io.github.drumber.kitsune.util.extensions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

const val SCROLL_JUMP_THRESHOLD_DP = 2500
const val SCROLL_JUMP_THRESHOLD_POS = 30

/**
 * Configures the view so that a confirmed single tap invokes [onSingleTap] (if provided) and a
 * double tap invokes [onDoubleTap]. Using a gesture detector ensures a single tap is only fired
 * once it is clear the user is not performing a double tap, so the two actions don't conflict.
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setOnDoubleTapListener(
    onSingleTap: (() -> Unit)? = null,
    onDoubleTap: () -> Unit
) {
    val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onSingleTap?.invoke()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTap()
                return true
            }
        }
    )

    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            if (onSingleTap != null && action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
                onSingleTap.invoke()
                return true
            }
            return super.performAccessibilityAction(host, action, args)
        }
    })

    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
        false
    }
}

fun NestedScrollView.smoothScrollOrJumpToTop() {
    if (scrollY > SCROLL_JUMP_THRESHOLD_DP.toPx()) {
        scrollTo(0, 0)
    } else {
        smoothScrollTo(0, 0)
    }
}

fun RecyclerView.smoothScrollOrJumpToTop() {
    val currentItemPosition = (layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()

    if (currentItemPosition != null && currentItemPosition > SCROLL_JUMP_THRESHOLD_POS) {
        scrollToPosition(0)
    } else {
        smoothScrollToPosition(0)
    }
}
