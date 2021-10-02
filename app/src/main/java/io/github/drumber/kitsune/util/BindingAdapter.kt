package io.github.drumber.kitsune.util

import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import at.blogc.android.views.ExpandableTextView
import io.github.drumber.kitsune.R

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("actionButton")
    fun setExpandCollapseButton(expandableTextView: ExpandableTextView, actionView: TextView) {
        expandableTextView.addOnExpandListener(object : ExpandableTextView.OnExpandListener {
            override fun onExpand(view: ExpandableTextView) {
                actionView.setText(R.string.action_read_less)
            }
            override fun onCollapse(view: ExpandableTextView) {
                actionView.setText(R.string.action_read_more)
            }
        })
        expandableTextView.post {
            actionView.isVisible = expandableTextView.lineCount >= expandableTextView.maxLines
        }
        actionView.setOnClickListener { expandableTextView.toggle() }
    }

}