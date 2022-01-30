package io.github.drumber.kitsune.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.os.bundleOf
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.widget.ExpandableLayout.State.*
import kotlin.math.round

/**
 * Layout for expanding and collapsing the views height with an optional minimum height.
 *
 * Modified version of https://github.com/cachapa/ExpandableLayout
 */
class ExpandableLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {

    enum class State {
        COLLAPSED,
        COLLAPSING,
        EXPANDING,
        EXPANDED
    }

    companion object {
        const val KEY_SUPER_STATE = "super_state"
        const val KEY_EXPANSION = "expansion"
    }

    var duration = 300

    private var expansion = 1f

    var minHeight = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    private var state = EXPANDED
        set(value) {
            field = value
            if (_expandedState.value != isExpanded()) {
                _expandedState.postValue(isExpanded())
            }
        }

    private val _expandedState = MutableLiveData(isExpanded())
    val expandedState get() = _expandedState as LiveData<Boolean>

    var interpolator = FastOutSlowInInterpolator()
    private var animator: ValueAnimator? = null

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.ExpandableLayout)
            duration = a.getInt(R.styleable.ExpandableLayout_duration, duration)
            expansion = if (a.getBoolean(R.styleable.ExpandableLayout_expanded, true)) 1f else 0f
            minHeight = a.getDimension(R.styleable.ExpandableLayout_min_height, minHeight)
            a.recycle()

            state = if (expansion == 0f && minHeight > 0f) COLLAPSED else EXPANDED
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        expansion = if (isExpanded()) 1f else 0f
        return bundleOf(
            KEY_EXPANSION to expansion,
            KEY_SUPER_STATE to superState
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null) {
            return super.onRestoreInstanceState(state)
        }

        val bundle = state as Bundle
        expansion = bundle.getFloat(KEY_EXPANSION)
        this.state = if (expansion == 1f) EXPANDED else COLLAPSED
        val superState = bundle.getParcelable<Parcelable>(KEY_SUPER_STATE)

        super.onRestoreInstanceState(superState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        val height = measuredHeight

        val minHeight = minHeight.coerceAtMost(height.toFloat())
        val calculatedHeight = (minHeight + round((height - minHeight) * expansion)).toInt()
        setMeasuredDimension(width, calculatedHeight)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        animator?.cancel()
        super.onConfigurationChanged(newConfig)
    }

    fun isExpanded() = state == EXPANDING || state == EXPANDED

    @JvmOverloads
    fun toggle(animate: Boolean = true) {
        if (isExpanded()) {
            collapse(animate)
        } else {
            expand(animate)
        }
    }

    fun expand(animate: Boolean = true) {
        setExpanded(true, animate)
    }

    fun collapse(animate: Boolean = true) {
        setExpanded(false, animate)
    }

    fun setExpanded(expand: Boolean, animate: Boolean = true) {
        if (expand == isExpanded()) return

        val targetExpansion = if (expand) 1f else 0f
        if (animate) {
            animateSize(targetExpansion)
        } else {
            setExpansion(targetExpansion)
        }
    }

    fun getExpansion() = expansion

    fun setExpansion(expansion: Float) {
        if (this.expansion == expansion) return

        val delta = expansion - this.expansion
        state = when {
            expansion == 0f -> COLLAPSED
            expansion == 1f -> EXPANDED
            delta < 0 -> COLLAPSING
            else -> EXPANDING
        }

        this.expansion = expansion
        requestLayout()
    }

    private fun animateSize(targetExpansion: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(expansion, targetExpansion).apply {
            interpolator = this@ExpandableLayout.interpolator
            duration = this@ExpandableLayout.duration.toLong()

            addUpdateListener {
                setExpansion(it.animatedValue as Float)
            }

            var canceled = false
            addListener(
                onStart = {
                    state = if (targetExpansion == 0f) COLLAPSING else EXPANDING
                },
                onEnd = {
                    if (!canceled) {
                        state = if (targetExpansion == 0f) COLLAPSED else EXPANDED
                    }
                },
                onCancel = { canceled = true }
            )

            start()
        }
    }

}