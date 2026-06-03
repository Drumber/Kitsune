package io.github.drumber.kitsune.util.extensions

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

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
    // Keep a click listener so accessibility services can still trigger the primary action.
    if (onSingleTap != null) {
        setOnClickListener { onSingleTap() }
    }
    setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
}
