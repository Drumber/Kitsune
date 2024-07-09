package io.github.drumber.kitsune.ui.component.algolia.range

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.number.range.Range
import com.google.android.material.slider.RangeSlider

class IntNumberRangeView(
    private val slider: RangeSlider
) : CustomNumberRangeView<Int> {

    override var onRangeChanged: Callback<Range<Int>?>? = null

    private var range: Range<Int>? = null
    private var bounds: Range<Int>? = null

    init {
        slider.stepSize = 1f
        slider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {}

            override fun onStopTrackingTouch(slider: RangeSlider) {
                val valueMin = slider.values[0].toInt()
                val valueMax = slider.values[1].toInt()
                if (valueMin == bounds?.min && valueMax == bounds?.max) {
                    onRangeChanged?.invoke(null)
                } else if (range?.min != valueMin || range?.max != valueMax) {
                    onRangeChanged?.invoke(Range(valueMin..valueMax))
                }
            }
        })
    }

    override fun setRange(range: Range<Int>?) {
        if (range == null) {
            bounds?.let {
                slider.setValues(it.min.toFloat(), it.max.toFloat())
            }
        } else if (this.range != range) {
            slider.setValues(range.min.toFloat(), range.max.toFloat())
        }
        this.range = range
    }

    override fun setBounds(bounds: Range<Int>?) {
        bounds?.let {
            this.bounds = it
            slider.valueFrom = it.min.toFloat()
            slider.valueTo = it.max.toFloat()
            slider.setValues(
                range?.min?.toFloat() ?: it.min.toFloat(),
                range?.max?.toFloat() ?: it.max.toFloat()
            )
        }
    }

}