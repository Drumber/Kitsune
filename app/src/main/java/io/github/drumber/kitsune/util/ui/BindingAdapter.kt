package io.github.drumber.kitsune.util.ui

import android.view.View
import androidx.appcompat.widget.TooltipCompat
import androidx.databinding.BindingAdapter

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("tooltip")
    fun tooltip(view: View, text: String) {
        TooltipCompat.setTooltipText(view, text)
    }

}
