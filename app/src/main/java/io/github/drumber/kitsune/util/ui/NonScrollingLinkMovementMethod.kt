package io.github.drumber.kitsune.util.ui

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

/**
 * A [LinkMovementMethod] that only consumes touch events landing on a [ClickableSpan].
 *
 * The stock [LinkMovementMethod] extends [android.text.method.ScrollingMovementMethod] and
 * consumes every touch event regardless of whether it hits a link.
 * This implementation returns `false` for touches outside a span so they propagate to the parent
 * and prevents scrolling, while still handling link clicks and selection exactly like [LinkMovementMethod].
 */
class NonScrollingLinkMovementMethod : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(off, off, ClickableSpan::class.java)

            if (links.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    links[0].onClick(widget)
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(links[0]), buffer.getSpanEnd(links[0]))
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }

        // Not on a link: don't consume it. Let it fall through to the parent
        return false
    }

    companion object {
        val instance: NonScrollingLinkMovementMethod by lazy { NonScrollingLinkMovementMethod() }
    }
}