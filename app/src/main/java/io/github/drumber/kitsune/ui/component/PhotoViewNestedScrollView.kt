package io.github.drumber.kitsune.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

/**
 * Sets the size of its children equal to its own size and manages scroll interceptions.
 */
class PhotoViewNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    companion object {
        private const val SCROLL_UP_THRESHOLD = 60f
    }

    private var disallowInterceptTouchEvents = false

    private var startY = -1f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!isFillViewport) {
            return
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return
        }

        if (childCount > 0) {
            val child = getChildAt(0)
            child.measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        disallowInterceptTouchEvents = disallowIntercept
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            startY = ev.y
        }
        val isScrollingUp = (startY != -1f && ev.y - startY > SCROLL_UP_THRESHOLD)
                || ev.action == MotionEvent.ACTION_DOWN // Hauler needs to receive the ACTION_DOWN event
        if (ev.action == MotionEvent.ACTION_UP) {
            startY = -1f
        }

        return !disallowInterceptTouchEvents
                && ev.pointerCount == 1 // prevent intercepting pinch-to-zoom gesture
                && isScrollingUp // only intercept scroll-up actions
                && super.onInterceptTouchEvent(ev)
    }

}