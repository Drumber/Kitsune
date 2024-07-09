package io.github.drumber.kitsune.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.CustomNumberSpinnerBinding

typealias ValueChangedListener = (Int) -> Unit

class CustomNumberSpinner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val binding: CustomNumberSpinnerBinding

    private var maxValue: Int = Int.MAX_VALUE
    private var minValue: Int = 0

    private var suffixMode = SuffixMode.Disabled
    private var customSuffixText: String? = null

    private var onValueChangedListener: ValueChangedListener? = null

    private var ignoreTextChangedEvent: Boolean = false

    init {
        val view = inflate(context, R.layout.custom_number_spinner, this)
        binding = CustomNumberSpinnerBinding.bind(view)

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.CustomNumberSpinner)
            binding.apply {
                layoutAction.isVisible =
                    a.getBoolean(R.styleable.CustomNumberSpinner_enableAction, false)
                btnAction.text = a.getString(R.styleable.CustomNumberSpinner_actionText)
                setActionTooltip(a.getString(R.styleable.CustomNumberSpinner_actionTooltip))

                fieldCount.setText(a.getInt(R.styleable.CustomNumberSpinner_value, 0).toString())
                maxValue = a.getInt(R.styleable.CustomNumberSpinner_maxValue, Int.MAX_VALUE)
                minValue = a.getInt(R.styleable.CustomNumberSpinner_minValue, 0)

                suffixMode =
                    SuffixMode.entries[a.getInt(R.styleable.CustomNumberSpinner_suffixMode, 0)]
                tvSuffix.isVisible = suffixMode != SuffixMode.Disabled
                customSuffixText = a.getString(R.styleable.CustomNumberSpinner_suffixText)
                updateSuffixText()
            }
            a.recycle()
        }

        binding.apply {
            fieldCount.doAfterTextChanged {
                if (ignoreTextChangedEvent) {
                    return@doAfterTextChanged
                }

                val value = it?.toString()?.toIntOrNull()?.coerceIn(minValue..maxValue) ?: minValue
                onValueChangedListener?.invoke(value)
            }

            btnDecrement.setOnClickListener {
                val value = getValue()?.minus(1) ?: minValue
                setValue(value)
                onValueChangedListener?.invoke(value)
            }
            btnIncrement.setOnClickListener {
                val value = (getValue() ?: minValue).plus(1)
                setValue(value)
                onValueChangedListener?.invoke(value)
            }
        }
    }

    private fun updateButtonState() {
        val value = getValue() ?: minValue
        binding.apply {
            btnDecrement.isEnabled = value > minValue
            btnIncrement.isEnabled = value < maxValue
        }
    }

    private fun updateSuffixText() {
        binding.tvSuffix.text = if (suffixMode == SuffixMode.MaxValue) {
            "/ $maxValue"
        } else {
            customSuffixText
        }
    }

    fun setValue(value: Int) {
        ignoreTextChangedEvent = true
        binding.fieldCount.apply {
            editableText.clear()
            append(value.coerceIn(minValue..maxValue).toString())
        }
        ignoreTextChangedEvent = false
        updateButtonState()
    }

    fun getValue() = binding.fieldCount.text?.toString()?.toIntOrNull()

    fun setValueChangedListener(onValueChanged: ValueChangedListener?) {
        onValueChangedListener = onValueChanged
    }

    fun setMaxValue(maxValue: Int) {
        this.maxValue = maxValue
        // make sure value is within new range
        getValue()?.let { setValue(it) }
        if (suffixMode == SuffixMode.MaxValue) {
            updateSuffixText()
        }
    }

    fun getMaxValue() = maxValue

    fun setMinValue(minValue: Int) {
        this.minValue = minValue
        // make sure value is within new range
        getValue()?.let { setValue(it) }
    }

    fun getMinValue() = minValue

    fun setSuffixMode(suffixMode: SuffixMode) {
        this.suffixMode = suffixMode
        binding.tvSuffix.isVisible = this.suffixMode != SuffixMode.Disabled
        updateSuffixText()
    }

    fun getSuffixModel() = suffixMode

    fun setSuffixCustomText(suffixText: String) {
        customSuffixText = suffixText
        if (suffixMode == SuffixMode.CustomText) {
            updateSuffixText()
        }
    }

    fun setActionEnabled(enableAction: Boolean) {
        binding.layoutAction.isVisible = enableAction
    }

    fun isActionEnabled() = binding.layoutAction.isVisible

    fun setActionText(actionText: String) {
        binding.btnAction.text = actionText
    }

    fun setActionTooltip(tooltip: String?) {
        TooltipCompat.setTooltipText(binding.btnAction, tooltip)
    }

    fun setActionClickListener(l: OnClickListener?) {
        binding.btnAction.setOnClickListener(l)
    }

    enum class SuffixMode {
        Disabled, MaxValue, CustomText
    }

}
